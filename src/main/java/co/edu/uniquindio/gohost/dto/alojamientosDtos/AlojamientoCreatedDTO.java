package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.model.ServicioAlojamiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta de creación de alojamiento.
 * Contiene solo los datos necesarios sin exponer la entidad completa.
 */
public record AlojamientoCreatedDTO(
        UUID id,
        String titulo,
        String descripcion,
        Direccion direccion,
        BigDecimal precioNoche,
        Integer capacidad,
        List<String> fotos,
        List<ServicioAlojamiento> servicios,
        Boolean activo,
        UUID anfitrionId,
        LocalDateTime fechaCreacion
) {}