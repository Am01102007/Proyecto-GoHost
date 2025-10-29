package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.model.*;
import co.edu.uniquindio.gohost.repository.NotificacionRecordatorioRepository;
import co.edu.uniquindio.gohost.service.RecordatorioService;
import co.edu.uniquindio.gohost.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de recordatorios automáticos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecordatorioServiceImpl implements RecordatorioService {

    private final NotificacionRecordatorioRepository recordatorioRepository;
    private final MailService mailService;

    @Value("${app.recordatorios.horas-antes-checkin:24}")
    private int horasAntesCheckin;

    @Value("${app.recordatorios.horas-dia-checkin:2}")
    private int horasDiaCheckin;

    @Value("${app.recordatorios.max-intentos:3}")
    private int maxIntentos;

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void programarRecordatoriosParaReserva(Reserva reserva) {
        log.info("Programando recordatorios para reserva: {}", reserva.getId());

        try {
            // Recordatorio para huésped 24 horas antes
            if (!recordatorioRepository.existsByReservaIdAndTipo(reserva.getId(), TipoRecordatorio.RECORDATORIO_HUESPED_CHECKIN)) {
                crearRecordatorio(reserva, TipoRecordatorio.RECORDATORIO_HUESPED_CHECKIN);
            }

            // Recordatorio para anfitrión 24 horas antes
            if (!recordatorioRepository.existsByReservaIdAndTipo(reserva.getId(), TipoRecordatorio.RECORDATORIO_ANFITRION_LLEGADA)) {
                crearRecordatorio(reserva, TipoRecordatorio.RECORDATORIO_ANFITRION_LLEGADA);
            }

            // Recordatorio para huésped el día del check-in
            if (!recordatorioRepository.existsByReservaIdAndTipo(reserva.getId(), TipoRecordatorio.RECORDATORIO_HUESPED_DIA_CHECKIN)) {
                crearRecordatorio(reserva, TipoRecordatorio.RECORDATORIO_HUESPED_DIA_CHECKIN);
            }

            // Recordatorio para anfitrión el día de la llegada
            if (!recordatorioRepository.existsByReservaIdAndTipo(reserva.getId(), TipoRecordatorio.RECORDATORIO_ANFITRION_DIA_LLEGADA)) {
                crearRecordatorio(reserva, TipoRecordatorio.RECORDATORIO_ANFITRION_DIA_LLEGADA);
            }

            log.info("Recordatorios programados exitosamente para reserva: {}", reserva.getId());
        } catch (Exception e) {
            log.error("Error al programar recordatorios para reserva {}: {}", reserva.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void cancelarRecordatoriosDeReserva(UUID reservaId) {
        log.info("Cancelando recordatorios para reserva: {}", reservaId);

        try {
            List<NotificacionRecordatorio> recordatorios = recordatorioRepository.findByReservaId(reservaId);
            
            for (NotificacionRecordatorio recordatorio : recordatorios) {
                if (recordatorio.getEstado() == EstadoRecordatorio.PROGRAMADO) {
                    recordatorio.setEstado(EstadoRecordatorio.CANCELADO);
                    recordatorio.setFechaActualizacion(LocalDateTime.now());
                    recordatorioRepository.save(recordatorio);
                }
            }

            log.info("Recordatorios cancelados para reserva: {}", reservaId);
        } catch (Exception e) {
            log.error("Error al cancelar recordatorios para reserva {}: {}", reservaId, e.getMessage(), e);
        }
    }

    @Override
    public void procesarRecordatoriosPendientes() {
        log.info("Procesando recordatorios pendientes...");

        try {
            List<NotificacionRecordatorio> recordatoriosPendientes = 
                recordatorioRepository.findNotificacionesPendientes(EstadoRecordatorio.PROGRAMADO, LocalDateTime.now());

            log.info("Encontrados {} recordatorios pendientes", recordatoriosPendientes.size());

            for (NotificacionRecordatorio recordatorio : recordatoriosPendientes) {
                enviarRecordatorio(recordatorio);
            }
        } catch (Exception e) {
            log.error("Error al procesar recordatorios pendientes: {}", e.getMessage(), e);
        }
    }

    @Override
    public NotificacionRecordatorio crearRecordatorio(Reserva reserva, TipoRecordatorio tipo) {
        LocalDateTime fechaProgramada = calcularFechaProgramada(reserva, tipo);
        Usuario destinatario = obtenerDestinatario(reserva, tipo);
        String asunto = generarAsunto(reserva, tipo);
        String mensaje = generarMensaje(reserva, tipo);

        NotificacionRecordatorio recordatorio = NotificacionRecordatorio.builder()
                .reserva(reserva)
                .destinatario(destinatario)
                .tipoRecordatorio(tipo)
                .estado(EstadoRecordatorio.PROGRAMADO)
                .fechaProgramada(fechaProgramada)
                .asunto(asunto)
                .mensaje(mensaje)
                .emailDestinatario(destinatario.getEmail())
                .intentosEnvio(0)
                .build();

        return recordatorioRepository.save(recordatorio);
    }

    @Override
    public boolean enviarRecordatorio(NotificacionRecordatorio recordatorio) {
        try {
            log.info("Enviando recordatorio {} a {}", recordatorio.getId(), recordatorio.getEmailDestinatario());

            // Incrementar intentos de envío
            recordatorio.setIntentosEnvio(recordatorio.getIntentosEnvio() + 1);

            // Enviar email
            mailService.sendMail(
                recordatorio.getEmailDestinatario(),
                recordatorio.getAsunto(),
                recordatorio.getMensaje()
            );

            // Marcar como enviado
            recordatorio.setEstado(EstadoRecordatorio.ENVIADO);
            recordatorio.setFechaEnviado(LocalDateTime.now());
            recordatorio.setMensajeError(null);

            recordatorioRepository.save(recordatorio);

            log.info("Recordatorio {} enviado exitosamente", recordatorio.getId());
            return true;

        } catch (Exception e) {
            log.error("Error al enviar recordatorio {}: {}", recordatorio.getId(), e.getMessage(), e);

            // Marcar como error si se agotaron los intentos
            if (recordatorio.getIntentosEnvio() >= maxIntentos) {
                recordatorio.setEstado(EstadoRecordatorio.ERROR);
            }
            
            recordatorio.setMensajeError(e.getMessage());
            recordatorioRepository.save(recordatorio);

            return false;
        }
    }

    @Override
    public List<NotificacionRecordatorio> obtenerRecordatoriosDeReserva(UUID reservaId) {
        return recordatorioRepository.findByReservaId(reservaId);
    }

    @Override
    public void reintentarRecordatoriosFallidos() {
        log.info("Reintentando recordatorios fallidos...");

        try {
            List<NotificacionRecordatorio> recordatoriosFallidos = 
                recordatorioRepository.findNotificacionesParaReintento(EstadoRecordatorio.PROGRAMADO, maxIntentos);

            log.info("Encontrados {} recordatorios para reintentar", recordatoriosFallidos.size());

            for (NotificacionRecordatorio recordatorio : recordatoriosFallidos) {
                if (recordatorio.getFechaProgramada().isBefore(LocalDateTime.now())) {
                    enviarRecordatorio(recordatorio);
                }
            }
        } catch (Exception e) {
            log.error("Error al reintentar recordatorios fallidos: {}", e.getMessage(), e);
        }
    }

    @Override
    public void marcarComoEnviado(UUID recordatorioId) {
        recordatorioRepository.findById(recordatorioId).ifPresent(recordatorio -> {
            recordatorio.setEstado(EstadoRecordatorio.ENVIADO);
            recordatorio.setFechaEnviado(LocalDateTime.now());
            recordatorioRepository.save(recordatorio);
        });
    }

    @Override
    public void marcarComoFallido(UUID recordatorioId, String mensajeError) {
        recordatorioRepository.findById(recordatorioId).ifPresent(recordatorio -> {
            recordatorio.setEstado(EstadoRecordatorio.ERROR);
            recordatorio.setMensajeError(mensajeError);
            recordatorioRepository.save(recordatorio);
        });
    }

    private LocalDateTime calcularFechaProgramada(Reserva reserva, TipoRecordatorio tipo) {
        LocalDateTime fechaCheckin = reserva.getCheckIn().atTime(14, 0); // Asumiendo check-in a las 14:00

        return switch (tipo) {
            case RECORDATORIO_HUESPED_CHECKIN, RECORDATORIO_ANFITRION_LLEGADA -> 
                fechaCheckin.minusHours(horasAntesCheckin);
            case RECORDATORIO_HUESPED_DIA_CHECKIN, RECORDATORIO_ANFITRION_DIA_LLEGADA -> 
                fechaCheckin.minusHours(horasDiaCheckin);
        };
    }

    private Usuario obtenerDestinatario(Reserva reserva, TipoRecordatorio tipo) {
        return switch (tipo) {
            case RECORDATORIO_HUESPED_CHECKIN, RECORDATORIO_HUESPED_DIA_CHECKIN -> 
                reserva.getHuesped();
            case RECORDATORIO_ANFITRION_LLEGADA, RECORDATORIO_ANFITRION_DIA_LLEGADA -> 
                reserva.getAlojamiento().getAnfitrion();
        };
    }

    private String generarAsunto(Reserva reserva, TipoRecordatorio tipo) {
        return switch (tipo) {
            case RECORDATORIO_HUESPED_CHECKIN -> 
                "Recordatorio: Tu check-in es mañana - " + reserva.getAlojamiento().getTitulo();
            case RECORDATORIO_HUESPED_DIA_CHECKIN -> 
                "¡Hoy es tu check-in! - " + reserva.getAlojamiento().getTitulo();
            case RECORDATORIO_ANFITRION_LLEGADA -> 
                "Recordatorio: Huésped llega mañana - " + reserva.getAlojamiento().getTitulo();
            case RECORDATORIO_ANFITRION_DIA_LLEGADA -> 
                "¡Hoy llega tu huésped! - " + reserva.getAlojamiento().getTitulo();
        };
    }

    private String generarMensaje(Reserva reserva, TipoRecordatorio tipo) {
        String fechaCheckin = reserva.getCheckIn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String alojamiento = reserva.getAlojamiento().getTitulo();
        String direccion = reserva.getAlojamiento().getDireccion().getCiudad();

        return switch (tipo) {
            case RECORDATORIO_HUESPED_CHECKIN -> String.format(
                "Hola %s,\n\n" +
                "Te recordamos que mañana (%s) tienes programado el check-in en:\n\n" +
                "🏠 %s\n" +
                "📍 %s\n\n" +
                "Por favor, asegúrate de llegar a tiempo y tener toda la documentación necesaria.\n\n" +
                "¡Esperamos que disfrutes tu estadía!\n\n" +
                "Saludos,\n" +
                "Equipo GoHost",
                reserva.getHuesped().getNombre(), fechaCheckin, alojamiento, direccion
            );

            case RECORDATORIO_HUESPED_DIA_CHECKIN -> String.format(
                "Hola %s,\n\n" +
                "¡Hoy es el día! Tu check-in está programado para hoy (%s) en:\n\n" +
                "🏠 %s\n" +
                "📍 %s\n\n" +
                "Recuerda llevar tu documentación y cualquier información adicional que te haya proporcionado el anfitrión.\n\n" +
                "¡Que tengas una excelente estadía!\n\n" +
                "Saludos,\n" +
                "Equipo GoHost",
                reserva.getHuesped().getNombre(), fechaCheckin, alojamiento, direccion
            );

            case RECORDATORIO_ANFITRION_LLEGADA -> String.format(
                "Hola %s,\n\n" +
                "Te recordamos que mañana (%s) tienes un huésped programado para check-in:\n\n" +
                "👤 Huésped: %s\n" +
                "🏠 Alojamiento: %s\n" +
                "📍 Ubicación: %s\n\n" +
                "Por favor, asegúrate de que el alojamiento esté listo para recibir al huésped.\n\n" +
                "Saludos,\n" +
                "Equipo GoHost",
                reserva.getAlojamiento().getAnfitrion().getNombre(), fechaCheckin, 
                reserva.getHuesped().getNombre(), alojamiento, direccion
            );

            case RECORDATORIO_ANFITRION_DIA_LLEGADA -> String.format(
                "Hola %s,\n\n" +
                "¡Hoy llega tu huésped! El check-in está programado para hoy (%s):\n\n" +
                "👤 Huésped: %s\n" +
                "🏠 Alojamiento: %s\n" +
                "📍 Ubicación: %s\n\n" +
                "Recuerda estar disponible para recibir al huésped y proporcionarle toda la información necesaria.\n\n" +
                "Saludos,\n" +
                "Equipo GoHost",
                reserva.getAlojamiento().getAnfitrion().getNombre(), fechaCheckin,
                reserva.getHuesped().getNombre(), alojamiento, direccion
            );
        };
    }
}