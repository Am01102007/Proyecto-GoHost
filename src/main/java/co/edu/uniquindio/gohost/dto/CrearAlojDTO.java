package co.edu.uniquindio.gohost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List; /** Crear alojamiento **/
public record CrearAlojDTO(
        @NotBlank String titulo,
        String descripcion,
        String ciudad,
        String pais,
        String calle,
        String zip,
        @Positive BigDecimal precioNoche,
        Integer capacidad,
        List<String> fotos
) {}
