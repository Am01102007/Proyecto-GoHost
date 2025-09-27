package co.edu.uniquindio.gohost.dto;

import java.time.LocalDate;
import java.util.UUID; /** Crear reserva **/
public record CrearReservaDTO(UUID alojamientoId, UUID huespedId, LocalDate checkIn, LocalDate checkOut) {}
