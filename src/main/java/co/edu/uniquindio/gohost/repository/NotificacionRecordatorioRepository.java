package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.EstadoRecordatorio;
import co.edu.uniquindio.gohost.model.NotificacionRecordatorio;
import co.edu.uniquindio.gohost.model.TipoRecordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para gestionar las operaciones de base de datos de NotificacionRecordatorio.
 */
@Repository
public interface NotificacionRecordatorioRepository extends JpaRepository<NotificacionRecordatorio, UUID> {

    /**
     * Encuentra todas las notificaciones programadas que deben ser enviadas.
     * @param fechaLimite Fecha límite hasta la cual buscar notificaciones programadas
     * @return Lista de notificaciones que deben ser enviadas
     */
    @Query("SELECT n FROM NotificacionRecordatorio n WHERE n.estado = :estado AND n.fechaProgramada <= :fechaLimite")
    List<NotificacionRecordatorio> findNotificacionesPendientes(
            @Param("estado") EstadoRecordatorio estado,
            @Param("fechaLimite") LocalDateTime fechaLimite
    );

    /**
     * Encuentra todas las notificaciones de una reserva específica.
     * @param reservaId ID de la reserva
     * @return Lista de notificaciones de la reserva
     */
    @Query("SELECT n FROM NotificacionRecordatorio n WHERE n.reserva.id = :reservaId")
    List<NotificacionRecordatorio> findByReservaId(@Param("reservaId") UUID reservaId);

    /**
     * Encuentra notificaciones por tipo y estado.
     * @param tipo Tipo de recordatorio
     * @param estado Estado del recordatorio
     * @return Lista de notificaciones que coinciden con los criterios
     */
    List<NotificacionRecordatorio> findByTipoRecordatorioAndEstado(
            TipoRecordatorio tipo, 
            EstadoRecordatorio estado
    );

    /**
     * Encuentra notificaciones que han fallado en el envío y necesitan reintento.
     * @param maxIntentos Número máximo de intentos permitidos
     * @return Lista de notificaciones que pueden ser reintentadas
     */
    @Query("SELECT n FROM NotificacionRecordatorio n WHERE n.estado = :estado AND n.intentosEnvio < :maxIntentos")
    List<NotificacionRecordatorio> findNotificacionesParaReintento(
            @Param("estado") EstadoRecordatorio estado,
            @Param("maxIntentos") Integer maxIntentos
    );

    /**
     * Verifica si ya existe una notificación para una reserva y tipo específico.
     * @param reservaId ID de la reserva
     * @param tipo Tipo de recordatorio
     * @return true si ya existe, false en caso contrario
     */
    @Query("SELECT COUNT(n) > 0 FROM NotificacionRecordatorio n WHERE n.reserva.id = :reservaId AND n.tipoRecordatorio = :tipo")
    boolean existsByReservaIdAndTipo(@Param("reservaId") UUID reservaId, @Param("tipo") TipoRecordatorio tipo);

    /**
     * Encuentra notificaciones por destinatario y estado.
     * @param destinatarioId ID del destinatario
     * @param estado Estado del recordatorio
     * @return Lista de notificaciones del destinatario
     */
    @Query("SELECT n FROM NotificacionRecordatorio n WHERE n.destinatario.id = :destinatarioId AND n.estado = :estado")
    List<NotificacionRecordatorio> findByDestinatarioIdAndEstado(
            @Param("destinatarioId") UUID destinatarioId,
            @Param("estado") EstadoRecordatorio estado
    );
}