package co.edu.uniquindio.gohost.dto.reservaDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/** Crear reserva **/
public record CrearReservaDTO(
        @NotNull(message = "El ID del alojamiento es obligatorio")
        UUID alojamientoId,
        
        UUID huespedId,
        
        @NotNull(message = "La fecha de check-in es obligatoria")
        LocalDate checkIn,
        
        @NotNull(message = "La fecha de check-out es obligatoria")
        LocalDate checkOut,
        
        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "El número de huéspedes debe ser al menos 1")
        Integer numeroHuespedes
) {}
