package co.edu.uniquindio.gohost.dto.reservaDtos;


import co.edu.uniquindio.gohost.model.EstadoReserva;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de lectura para devolver una reserva sin exponer entidades JPA.
 * Se usa en endpoints que listan, buscan o detallan reservas.
 */
public record ReservaResDTO(
        UUID id,
        LocalDate checkIn,
        LocalDate checkOut,
        EstadoReserva estado,
        boolean eliminada,
        UUID huespedId,
        String nombreHuesped,        // solo nombre
        UUID alojamientoId,
        String tituloAlojamiento,    // solo título
        String ciudadAlojamiento     // solo ciudad
) {}