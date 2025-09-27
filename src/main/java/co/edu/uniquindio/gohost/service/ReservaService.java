
package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/** Reglas de negocio de Reserva **/
public interface ReservaService {
    Reserva crear(UUID alojamientoId, UUID huespedId, LocalDate in, LocalDate out);
    Page<Reserva> listarPorHuesped(UUID huespedId, Pageable pageable);
    Page<Reserva> listarPorAnfitrion(UUID anfitrionId, Pageable pageable);
    Reserva actualizar(UUID id, LocalDate in, LocalDate out, EstadoReserva estado);
    void cancelar(UUID id);
    Reserva obtener(UUID id);
}
