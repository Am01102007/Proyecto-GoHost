package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repositorio para {@link Reserva}.
 * Incluye utilidades para detección de traslapes y listados por huésped/anfitrión.
 */
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    /**
     * Verifica si existe un traslape entre el rango propuesto [inicio, fin)
     * y reservas activas del mismo alojamiento.
     *
     * Condición de traslape usada:
     *   r.checkIn < :fin AND r.checkOut > :inicio
     * (intervalo semiabierto; evita falsos positivos cuando una reserva termina
     *  exactamente el día que empieza otra).
     *
     * Se excluyen:
     *  - reservas eliminadas (eliminada = true)
     *  - reservas con estado CANCELADA
     *
     * @param alojamientoId id del alojamiento
     * @param inicio        fecha de check-in propuesta (inclusive)
     * @param fin           fecha de check-out propuesta (exclusiva)
     * @return true si hay traslape, false si está disponible
     */
    @Query("""
        SELECT (COUNT(r) > 0)
          FROM Reserva r
         WHERE r.alojamiento.id = :alojamientoId
           AND r.eliminada = false
           AND r.estado <> co.edu.uniquindio.gohost.model.EstadoReserva.CANCELADA
           AND r.checkIn < :fin
           AND r.checkOut > :inicio
    """)
    boolean existsTraslape(@Param("alojamientoId") UUID alojamientoId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fin") LocalDate fin);

    /**
     * Lista reservas hechas por un huésped específico.
     *
     * @param huespedId id del huésped
     * @param pageable  configuración de paginación
     * @return página de reservas
     */
    Page<Reserva> findByHuespedId(@Param("huespedId") UUID huespedId, Pageable pageable);

    /**
     * Lista reservas de los alojamientos pertenecientes a un anfitrión.
     *
     * @param anfitrionId id del anfitrión
     * @param pageable    configuración de paginación
     * @return página de reservas
     */
    Page<Reserva> findByAlojamientoAnfitrionId(@Param("anfitrionId") UUID anfitrionId, Pageable pageable);

    /**
     * Cuenta cuántas reservas de un anfitrión están en un estado específico.
     *
     * @param anfitrionId id del anfitrión
     * @param estado      estado de la reserva
     * @return número de reservas en ese estado
     */
    long countByAlojamientoAnfitrionIdAndEstado(@Param("anfitrionId") UUID anfitrionId,
                                                @Param("estado") EstadoReserva estado);
}
