package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para {@link Reserva}.
 * Incluye utilidades para detección de traslapes y listados por huésped/anfitrión
 * con JOIN FETCH para evitar LazyInitializationException.
 */
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    /**
     * Verifica si existe un traslape entre el rango propuesto [inicio, fin)
     * y reservas activas del mismo alojamiento.
     */
    @Query("""
        SELECT (COUNT(r) > 0)
          FROM Reserva r
         WHERE r.alojamiento.id = :alojamientoId
           AND r.eliminada = false
           AND r.estado <> 'CANCELADA'
           AND r.checkIn < :fin
           AND r.checkOut > :inicio
    """)
    boolean existsTraslape(@Param("alojamientoId") UUID alojamientoId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fin") LocalDate fin);

    /**
     * Verifica si el alojamiento tiene reservas futuras activas.
     */
    @Query("""
        SELECT (COUNT(r) > 0)
          FROM Reserva r
         WHERE r.alojamiento.id = :alojamientoId
           AND r.eliminada = false
           AND r.estado <> 'CANCELADA'
           AND r.checkIn >= :fechaActual
    """)
    boolean existsReservasFuturas(@Param("alojamientoId") UUID alojamientoId,
                                  @Param("fechaActual") LocalDate fechaActual);

    /* =========================================================
       Métodos con JOIN FETCH para evitar LazyInitializationException
       ========================================================= */

    /**
     * Lista reservas de un huésped con datos cargados (huesped, alojamiento, dirección, fotos).
     * NOTA: fetch join de colección + paginación puede forzar paginación en memoria.
     */
    @Query(value = """
        SELECT DISTINCT r FROM Reserva r
        JOIN FETCH r.huesped
        JOIN FETCH r.alojamiento a
        LEFT JOIN FETCH a.direccion
        -- fotos fuera del fetch para evitar explosión de filas
        WHERE r.huesped.id = :huespedId
        AND (:fechaInicio IS NULL OR r.checkIn >= :fechaInicio)
        AND (:fechaFin IS NULL OR r.checkOut <= :fechaFin)
        AND (:estado IS NULL OR r.estado = :estado)
        ORDER BY r.checkIn DESC
        """,
            countQuery = """
        SELECT COUNT(r) FROM Reserva r
        WHERE r.huesped.id = :huespedId
        AND (:fechaInicio IS NULL OR r.checkIn >= :fechaInicio)
        AND (:fechaFin IS NULL OR r.checkOut <= :fechaFin)
        AND (:estado IS NULL OR r.estado = :estado)
        """)
    Page<Reserva> findByHuespedIdWithFotos(@Param("huespedId") UUID huespedId, 
                                           @Param("fechaInicio") LocalDate fechaInicio,
                                           @Param("fechaFin") LocalDate fechaFin,
                                           @Param("estado") EstadoReserva estado,
                                           Pageable pageable);

    /**
     * Lista reservas de alojamientos de un anfitrión con datos cargados (huesped, alojamiento, dirección, fotos).
     * NOTA: fetch join de colección + paginación puede forzar paginación en memoria.
     */
    @Query(value = """
        SELECT DISTINCT r FROM Reserva r
        JOIN FETCH r.huesped
        JOIN FETCH r.alojamiento a
        LEFT JOIN FETCH a.direccion
        -- fotos fuera del fetch para evitar explosión de filas
        WHERE a.anfitrion.id = :anfitrionId
        """,
            countQuery = """
        SELECT COUNT(r) FROM Reserva r
        JOIN r.alojamiento a
        WHERE a.anfitrion.id = :anfitrionId
        """)
    Page<Reserva> findByAlojamientoAnfitrionIdWithFotos(@Param("anfitrionId") UUID anfitrionId, Pageable pageable);

    /**
     * Lista reservas de un alojamiento específico con datos cargados (huesped, alojamiento, dirección, fotos).
     * NOTA: fetch join de colección + paginación puede forzar paginación en memoria.
     */
    @Query(value = """
        SELECT DISTINCT r FROM Reserva r
        JOIN FETCH r.huesped
        JOIN FETCH r.alojamiento a
        LEFT JOIN FETCH a.direccion
        -- fotos fuera del fetch para evitar explosión de filas
        WHERE a.id = :alojamientoId
        """,
            countQuery = """
        SELECT COUNT(r) FROM Reserva r
        WHERE r.alojamiento.id = :alojamientoId
        """)
    Page<Reserva> findByAlojamientoIdWithFotos(@Param("alojamientoId") UUID alojamientoId, Pageable pageable);

    /**
     * Obtiene una reserva por ID con todas las relaciones necesarias cargadas (huesped, alojamiento, dirección, fotos).
     */
    @Query("""
        SELECT DISTINCT r FROM Reserva r
        JOIN FETCH r.huesped
        JOIN FETCH r.alojamiento a
        LEFT JOIN FETCH a.direccion
        LEFT JOIN FETCH a.fotos
        WHERE r.id = :id
    """)
    Optional<Reserva> findByIdWithFotos(@Param("id") UUID id);

    /* =========================================================
       Métodos anteriores (sin JOIN FETCH) – se conservan por compatibilidad
       ========================================================= */

    Page<Reserva> findByHuespedId(@Param("huespedId") UUID huespedId, Pageable pageable);

    Page<Reserva> findByAlojamientoAnfitrionId(@Param("anfitrionId") UUID anfitrionId, Pageable pageable);

    long countByAlojamientoAnfitrionIdAndEstado(@Param("anfitrionId") UUID anfitrionId,
                                                @Param("estado") EstadoReserva estado);
}
