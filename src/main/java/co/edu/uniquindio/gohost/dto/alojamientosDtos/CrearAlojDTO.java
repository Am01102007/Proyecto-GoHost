package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.Direccion;
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
) {public Direccion toDireccion() {
    return Direccion.builder()
            .ciudad(ciudad)
            .pais(pais)
            .calle(calle)
            .zip(zip)
            // Las coordenadas se calculan automáticamente
            .build();
}}

