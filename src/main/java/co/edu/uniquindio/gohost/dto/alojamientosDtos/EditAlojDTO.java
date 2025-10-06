package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.Direccion;

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
) {
    public Direccion toDireccion() {
        if (ciudad == null && pais == null && calle == null && zip == null) {
            return null;
        }
        return Direccion.builder()
                .ciudad(ciudad)
                .pais(pais)
                .calle(calle)
                .zip(zip)
                .build();
    }
}