package co.edu.uniquindio.gohost.dto;

import java.math.BigDecimal;
import java.util.List; /** Editar alojamiento **/
public record EditAlojDTO(
        String titulo,
        String descripcion,
        String ciudad,
        String pais,
        String calle,
        String zip,
        BigDecimal precioNoche,
        Integer capacidad,
        List<String> fotos,
        Boolean activo
) {}
