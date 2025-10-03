package co.edu.uniquindio.gohost.dto.reservaDtos;

import co.edu.uniquindio.gohost.model.EstadoReserva;

import java.time.LocalDate; /** Actualizar reserva **/
public record ActualizarReservaDTO(
        LocalDate checkIn,
        LocalDate checkOut,
        EstadoReserva estado) {

}
