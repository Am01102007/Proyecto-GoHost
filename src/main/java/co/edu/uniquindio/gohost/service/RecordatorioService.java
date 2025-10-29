package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.NotificacionRecordatorio;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.model.TipoRecordatorio;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar recordatorios automáticos del sistema.
 */
public interface RecordatorioService {

    /**
     * Programa todos los recordatorios para una reserva nueva.
     * @param reserva La reserva para la cual programar recordatorios
     */
    void programarRecordatoriosParaReserva(Reserva reserva);

    /**
     * Cancela todos los recordatorios pendientes de una reserva.
     * @param reservaId ID de la reserva
     */
    void cancelarRecordatoriosDeReserva(UUID reservaId);

    /**
     * Procesa y envía todos los recordatorios pendientes.
     * Este método es llamado por el scheduler automático.
     */
    void procesarRecordatoriosPendientes();

    /**
     * Crea un recordatorio específico para una reserva.
     * @param reserva La reserva
     * @param tipo Tipo de recordatorio
     * @return El recordatorio creado
     */
    NotificacionRecordatorio crearRecordatorio(Reserva reserva, TipoRecordatorio tipo);

    /**
     * Envía un recordatorio específico.
     * @param recordatorio El recordatorio a enviar
     * @return true si se envió exitosamente, false en caso contrario
     */
    boolean enviarRecordatorio(NotificacionRecordatorio recordatorio);

    /**
     * Obtiene todos los recordatorios de una reserva.
     * @param reservaId ID de la reserva
     * @return Lista de recordatorios
     */
    List<NotificacionRecordatorio> obtenerRecordatoriosDeReserva(UUID reservaId);

    /**
     * Reintenta el envío de recordatorios fallidos.
     */
    void reintentarRecordatoriosFallidos();

    /**
     * Marca un recordatorio como enviado.
     * @param recordatorioId ID del recordatorio
     */
    void marcarComoEnviado(UUID recordatorioId);

    /**
     * Marca un recordatorio como fallido.
     * @param recordatorioId ID del recordatorio
     * @param mensajeError Mensaje de error
     */
    void marcarComoFallido(UUID recordatorioId, String mensajeError);
}