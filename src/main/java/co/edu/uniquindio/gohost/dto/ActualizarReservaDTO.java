package co.edu.uniquindio.gohost.dto;

import co.edu.uniquindio.gohost.model.EstadoReserva;

import java.time.LocalDate; /** Actualizar reserva **/
public record ActualizarReservaDTO(LocalDate checkIn, LocalDate checkOut, EstadoReserva estado) {}
