package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.Direccion;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * EditAlojDTO — Datos opcionales para editar parcialmente un alojamiento
 * ============================================================================
 *
 * Uso típico en PATCH /api/alojamientos/{id}:
 *  - Solo los campos no nulos serán considerados para actualización.
 *  - Las validaciones se aplican únicamente si el campo viene presente (no nulo).
 */
public record EditAlojDTO(

        /** Título (opcional). Máx 200 caracteres */
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo,

        /** Descripción (opcional). Máx 4000 caracteres */
        @Size(max = 4000, message = "La descripción no puede superar los 4000 caracteres")
        String descripcion,

        /** Ciudad (opcional) */
        String ciudad,

        /** País (opcional) */
        String pais,

        /** Calle (opcional) */
        String calle,

        /** Código postal (opcional) */
        String zip,

        /** Precio por noche (opcional). Debe ser > 0 si viene */
        @Positive(message = "El precio debe ser mayor que cero")
        BigDecimal precioNoche,

        /** Capacidad (opcional). Debe ser > 0 si viene */
        @Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacidad,

        /** Lista de URLs de fotos (opcional). Si viene nula, no se modifica. Si viene, debe tener entre 1 y 10 fotos */
        @Size(min = 1, max = 10, message = "Debe proporcionar entre 1 y 10 fotos")
        List<String> fotos,

        /** Activación/desactivación (opcional) */
        Boolean activo
) {
    /**
     * Construye una Direccion solo si hay al menos un campo relacionado no nulo.
     * Si todos son nulos, retorna null para que la capa de servicio ignore el cambio.
     */
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

    /**
     * Devuelve una lista no nula. Útil si deseas reemplazar totalmente las fotos
     * cuando el cliente envía un arreglo (posiblemente vacío) y quieres evitar NPE.
     * Si el campo vino como null en el payload, devuelve null para indicar "no cambiar".
     */
    public List<String> getFotosOrNullOrEmptyCopy() {
        if (fotos == null) return null;           // no cambiar en BD
        return new ArrayList<>(fotos);            // reemplazar (posible lista vacía)
    }

    /**
     * Indica si se envió algún cambio relacionado con la dirección.
     */
    public boolean hasDireccionChanges() {
        return ciudad != null || pais != null || calle != null || zip != null;
    }
}