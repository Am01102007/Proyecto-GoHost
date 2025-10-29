package co.edu.uniquindio.gohost.model;

/**
 * Enumeración que define los tipos de recordatorios disponibles en el sistema.
 */
public enum TipoRecordatorio {
    
    /** Recordatorio para el huésped antes del check-in */
    RECORDATORIO_HUESPED_CHECKIN,
    
    /** Recordatorio para el anfitrión antes de la llegada del huésped */
    RECORDATORIO_ANFITRION_LLEGADA,
    
    /** Recordatorio para el huésped el día del check-in */
    RECORDATORIO_HUESPED_DIA_CHECKIN,
    
    /** Recordatorio para el anfitrión el día de la llegada */
    RECORDATORIO_ANFITRION_DIA_LLEGADA
}