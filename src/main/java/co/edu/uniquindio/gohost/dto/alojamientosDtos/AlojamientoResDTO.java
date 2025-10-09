package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de lectura para devolver alojamientos sin proxies ni entidades JPA.
 * Se usa solo en los endpoints que listan o buscan.
 */
public record AlojamientoResDTO(
        UUID id,
        String titulo,
        String descripcion,
        BigDecimal precioNoche,
        Integer capacidad,
        List<String> fotos,
        String ciudad,
        UUID anfitrionId
) {}