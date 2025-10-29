package co.edu.uniquindio.gohost.model;

/**
 * Enumeración que define los estados posibles de un recordatorio.
 */
public enum EstadoRecordatorio {
    
    /** Recordatorio programado pero aún no enviado */
    PROGRAMADO,
    
    /** Recordatorio enviado exitosamente */
    ENVIADO,
    
    /** Error al enviar el recordatorio */
    ERROR,
    
    /** Recordatorio cancelado (por ejemplo, si se cancela la reserva) */
    CANCELADO
}