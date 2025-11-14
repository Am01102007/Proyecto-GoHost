package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.ReservaResDTO;
import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.ReservaService;
import co.edu.uniquindio.gohost.service.RecordatorioService;
import co.edu.uniquindio.gohost.service.mail.MailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementación JPA de {@link ReservaService}.
 * Reglas:
 *  - Rango válido: in < out (intervalo semiabierto [in, out)).
 *  - No traslapar con reservas activas (no eliminadas y no CANCELADAS).
 *  - No modificar reservas eliminadas o CANCELADAS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final AlojamientoRepository alojRepo;
    private final MailService mailService;
    private final RecordatorioService recordatorioService;

    /** Crear una reserva nueva (retorna ENTIDAD). */
    @Override
    @Transactional
    public Reserva crear(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out) {
        validarRango(in, out);

        if (repo.existsTraslape(alojamientoId, in, out)) {
            throw new IllegalStateException("Fechas no disponibles");
        }

        var huesped = usuarioRepo.findById(huespedId)
                .orElseThrow(() -> new EntityNotFoundException("Huésped no existe"));

        var alojamiento = alojRepo.findById(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));

        var reserva = repo.save(Reserva.builder()
                .huesped(huesped)
                .alojamiento(alojamiento)
                .checkIn(in)
                .checkOut(out)
                .numeroHuespedes(1) // Valor por defecto para compatibilidad
                .estado(EstadoReserva.PENDIENTE)
                .eliminada(false)
                .build());

        // ========= Envío de correo de confirmación al huésped =========
        enviarCorreoConfirmacionHuesped(huesped, alojamiento, reserva, in, out);

        // ========= Programar recordatorios automáticos =========
        try {
            recordatorioService.programarRecordatoriosParaReserva(reserva);
        } catch (Exception e) {
            // Log del error pero no fallar la creación de la reserva
            log.error("Error al programar recordatorios para reserva {}: {}", reserva.getId(), e.getMessage(), e);
        }

        return reserva;
    }

    /** Crear una reserva y retornar DTO (con alojamiento/fotos inicializados). */
    @Override
    @Transactional
    public ReservaResDTO crearConDTO(UUID huespedId, CrearReservaDTO dto) {
        // Validar que el número de huéspedes no sea nulo
        if (dto.numeroHuespedes() == null) {
            throw new IllegalArgumentException("El número de huéspedes es obligatorio");
        }
        
        // Validar que el número de huéspedes no exceda la capacidad del alojamiento
        var alojamiento = alojRepo.findById(dto.alojamientoId())
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));
        
        if (dto.numeroHuespedes() > alojamiento.getCapacidad()) {
            throw new IllegalArgumentException("El número de huéspedes (" + dto.numeroHuespedes() + 
                ") excede la capacidad del alojamiento (" + alojamiento.getCapacidad() + ")");
        }
        
        // 1) crear entidad con la lógica existente pero incluyendo número de huéspedes
        Reserva creada = crearConHuespedes(dto.alojamientoId(), huespedId, dto.checkIn(), dto.checkOut(), dto.numeroHuespedes());

        // 2) recargar con JOIN FETCH para inicializar alojamiento.fotos (evita LazyInitializationException)
        Reserva completa = repo.findByIdWithFotos(creada.getId())
                .orElseThrow(() -> new EntityNotFoundException("Reserva recién creada no encontrada"));

        // 3) mapear a DTO
        return toRes(completa);
    }
    
    /** Método auxiliar para crear reserva con número de huéspedes */
    @Transactional
    private Reserva crearConHuespedes(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out, Integer numeroHuespedes) {
        validarRango(in, out);

        if (repo.existsTraslape(alojamientoId, in, out)) {
            throw new IllegalStateException("Fechas no disponibles");
        }

        var huesped = usuarioRepo.findById(huespedId)
                .orElseThrow(() -> new EntityNotFoundException("Huésped no existe"));

        var alojamiento = alojRepo.findById(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));

        var reserva = repo.save(Reserva.builder()
                .huesped(huesped)
                .alojamiento(alojamiento)
                .checkIn(in)
                .checkOut(out)
                .numeroHuespedes(numeroHuespedes)
                .estado(EstadoReserva.PENDIENTE)
                .eliminada(false)
                .build());

        // ========= Envío de correo de confirmación al huésped =========
        enviarCorreoConfirmacionHuesped(huesped, alojamiento, reserva, in, out);
        
        // ========= Envío de correo de notificación al anfitrión =========
        enviarCorreoNotificacionAnfitrion(alojamiento.getAnfitrion(), huesped, alojamiento, reserva, in, out, numeroHuespedes);

        return reserva;
    }

    /** Listar reservas del huésped autenticado como DTO con filtros y ordenamiento. */
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResDTO> listarPorHuespedConDTO(UUID huespedId, LocalDate fechaInicio, LocalDate fechaFin, EstadoReserva estado, Pageable pageable) {
        return repo.findByHuespedIdWithFotos(huespedId, fechaInicio, fechaFin, estado, pageable).map(this::toRes);
    }

    /** Listar reservas de los alojamientos del anfitrión autenticado como DTO. */
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResDTO> listarPorAnfitrionConDTO(UUID anfitrionId, Pageable pageable) {
        return repo.findByAlojamientoAnfitrionIdWithFotos(anfitrionId, pageable).map(this::toRes);
    }

    /** Listar reservas de un alojamiento específico como DTO con validación de autorización. */
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResDTO> listarPorAlojamientoConDTO(UUID alojamientoId, UUID anfitrionId, Pageable pageable) {
        // Validar que el alojamiento existe y pertenece al anfitrión autenticado
        var alojamiento = alojRepo.findById(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));
        
        if (!alojamiento.getAnfitrion().getId().equals(anfitrionId)) {
            throw new IllegalArgumentException("No tienes permisos para ver las reservas de este alojamiento");
        }
        
        return repo.findByAlojamientoIdWithFotos(alojamientoId, pageable).map(this::toRes);
    }

    /** Obtener una reserva por ID como DTO. */
    @Override
    @Transactional(readOnly = true)
    public ReservaResDTO obtenerConDTO(UUID id) {
        Reserva r = repo.findByIdWithFotos(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        return toRes(r);
    }

    /** Actualizar una reserva y devolver DTO. */
    @Override
    @Transactional
    public ReservaResDTO actualizarConDTO(UUID id, LocalDate in, LocalDate out, EstadoReserva estado) {
        Reserva actualizada = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        if (actualizada.isEliminada() || actualizada.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva cancelada/eliminada no puede modificarse");
        }
        EstadoReserva previo = actualizada.getEstado();

        if (in != null && out != null) {
            validarRango(in, out);
            if (repo.existsTraslape(actualizada.getAlojamiento().getId(), in, out)) {
                throw new IllegalStateException("Fechas no disponibles");
            }
            actualizada.setCheckIn(in);
            actualizada.setCheckOut(out);
        }

        if (estado != null) {
            actualizada.setEstado(estado);
        }

        repo.save(actualizada);
        Reserva cargada = repo.findByIdWithFotos(actualizada.getId()).orElseThrow();
        if (previo != cargada.getEstado()) {
            if (cargada.getEstado() == EstadoReserva.CONFIRMADA) {
                try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaConfirmadaHuesped(cargada)); } catch (Exception ignored) {}
                try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaConfirmadaAnfitrion(cargada)); } catch (Exception ignored) {}
            } else if (cargada.getEstado() == EstadoReserva.CANCELADA) {
                try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaCanceladaHuesped(cargada)); } catch (Exception ignored) {}
                try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaCanceladaAnfitrion(cargada)); } catch (Exception ignored) {}
            }
        }
        return toRes(cargada);
    }

    /** Cancelar (soft delete + estado CANCELADA). Idempotente. */
    @Override
    @Transactional
    public void cancelar(UUID id) {
        var r = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
        if (r.getEstado() == EstadoReserva.CANCELADA && r.isEliminada()) {
            return; // idempotente
        }
        
        // Validar que no se pueda cancelar con menos de 48 horas de anticipación
        LocalDate fechaLimite = LocalDate.now().plusDays(2); // 48 horas = 2 días
        if (r.getCheckIn().isBefore(fechaLimite)) {
            throw new IllegalStateException("No se puede cancelar la reserva con menos de 48 horas de anticipación");
        }
        
        r.setEstado(EstadoReserva.CANCELADA);
        r.setEliminada(true);
        repo.save(r);
        try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaCanceladaHuesped(r)); } catch (Exception ignored) {}
        try { mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.reservaCanceladaAnfitrion(r)); } catch (Exception ignored) {}

        // ========= Cancelar recordatorios automáticos =========
        try {
            recordatorioService.cancelarRecordatoriosDeReserva(id);
        } catch (Exception e) {
            // Log del error pero no fallar la cancelación de la reserva
            log.error("Error al cancelar recordatorios para reserva {}: {}", id, e.getMessage(), e);
        }
    }

    /** Obtener una reserva por ID (entidad). */
    @Transactional(readOnly = true)
    @Override
    public Reserva obtener(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no existe"));
    }

    /** Utilidad: valida que in < out y que no sean fechas pasadas. */
    private void validarRango(LocalDate in, LocalDate out) {
        if (in == null || out == null || !out.isAfter(in)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        
        // Validar que no se puedan crear reservas en fechas pasadas (permitir desde hoy)
        if (in.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden crear reservas en fechas pasadas");
        }
    }
    
    /** Método auxiliar para enviar correo de confirmación al huésped */
    private void enviarCorreoConfirmacionHuesped(Usuario huesped, Alojamiento alojamiento, Reserva reserva, LocalDate in, LocalDate out) {
        final String emailDestino = huesped.getEmail();
        final String nombreHuesped = huesped.getNombre();
        final String tituloAloj = alojamiento.getTitulo();
        final long noches = ChronoUnit.DAYS.between(in, out);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = in.format(fmt);
        String checkOutStr = out.format(fmt);

        try {
            mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.confirmacionReservaHuesped(huesped, alojamiento, reserva, in, out));
        } catch (Exception e) {
            // Log del error pero no fallar la creación de la reserva por problemas de correo
            log.error("Error al enviar correo de confirmación al huésped {}: {}", huesped.getId(), e.getMessage(), e);
        }
    }
    
    /** Método auxiliar para enviar correo de notificación al anfitrión */
    private void enviarCorreoNotificacionAnfitrion(Usuario anfitrion, Usuario huesped, Alojamiento alojamiento, Reserva reserva, LocalDate in, LocalDate out, Integer numeroHuespedes) {
        final String emailAnfitrion = anfitrion.getEmail();
        final String nombreAnfitrion = anfitrion.getNombre();
        final String nombreHuesped = huesped.getNombre();
        final String emailHuesped = huesped.getEmail();
        final String tituloAloj = alojamiento.getTitulo();
        final long noches = ChronoUnit.DAYS.between(in, out);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = in.format(fmt);
        String checkOutStr = out.format(fmt);

        try {
            mailService.sendAsync(co.edu.uniquindio.gohost.service.mail.MailTemplates.nuevaReservaAnfitrion(anfitrion, huesped, alojamiento, reserva, in, out, numeroHuespedes));
        } catch (Exception e) {
            // Log el error pero no fallar la reserva por problemas de correo
            log.error("Error al enviar correo al anfitrión {} para reserva {}: {}", anfitrion.getId(), reserva.getId(), e.getMessage(), e);
        }
    }

    /* =========================================================
       MAPEO MANUAL: Reserva → ReservaResDTO
       ========================================================= */

    private ReservaResDTO toRes(Reserva r) {
        return new ReservaResDTO(
                r.getId(),
                r.getCheckIn(),
                r.getCheckOut(),
                r.getEstado() != null ? EstadoReserva.valueOf(r.getEstado().name()) : null,
                r.isEliminada(),
                r.getHuesped() != null ? r.getHuesped().getId() : null,
                r.getHuesped() != null ? r.getHuesped().getNombre() : null, // asume getNombre()
                r.getAlojamiento() != null ? r.getAlojamiento().getId() : null,
                r.getAlojamiento() != null ? r.getAlojamiento().getTitulo() : null,
                (r.getAlojamiento() != null && r.getAlojamiento().getDireccion() != null)
                        ? r.getAlojamiento().getDireccion().getCiudad()
                        : "Sin ciudad"
        );
    }
}
