
package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/** Repositorio de reservas, incluye verificación de traslapes **/
public interface ReservaRepository extends JpaRepository<Reserva, java.util.UUID> {

    /** Indica si hay traslape con el rango propuesto **/
    @Query("""
        select (count(r) > 0) from Reserva r
        where r.alojamiento.id = :alojId and r.eliminada = false
          and r.estado <> co.edu.uniquindio.gohost.model.EstadoReserva.CANCELADA
          and (r.checkIn <= :fin and r.checkOut > :inicio)
    """)
    boolean existsTraslape(@Param("alojId") java.util.UUID alojamientoId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fin") LocalDate fin);

    /** Por huésped **/
    Page<Reserva> findByHuespedId(java.util.UUID huespedId, Pageable pageable);

    /** Por anfitrión **/
    Page<Reserva> findByAlojamientoAnfitrionId(java.util.UUID anfitrionId, Pageable pageable);

    /** Conteo por estado **/
    long countByAlojamientoAnfitrionIdAndEstado(java.util.UUID anfitrionId, EstadoReserva estado);
}
