package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.model.ServicioAlojamiento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * CrearAlojDTO — Datos requeridos para crear un alojamiento
 * ============================================================================
 *
 * - Se usa en el endpoint POST /api/alojamientos
 * - Permite construir automáticamente la entidad {@link co.edu.uniquindio.gohost.model.Alojamiento}
 * - Incluye la dirección embebida y una lista de URLs de fotos (subidas previamente)
 */
public record CrearAlojDTO(

        /** Título descriptivo del alojamiento */
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo,

        /** Descripción larga opcional */
        @Size(max = 4000, message = "La descripción no puede superar los 4000 caracteres")
        String descripcion,

        /** Ciudad donde se ubica el alojamiento */
        @NotBlank(message = "La ciudad es obligatoria")
        String ciudad,

        /** País de ubicación */
        @NotBlank(message = "El país es obligatorio")
        String pais,

        /** Calle y número */
        @NotBlank(message = "La calle es obligatoria")
        String calle,

        /** Código postal (opcional) */
        String zip,

        /** Precio por noche */
        @NotNull(message = "El precio por noche es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        BigDecimal precioNoche,

        /** Capacidad máxima de huéspedes */
        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacidad,

        /** URLs de imágenes (Cloudinary u otro proveedor). En flujo multipart se generan automáticamente. */
        @Size(max = 10, message = "No puede superar 10 fotos")
        List<String> fotos,

        /** Servicios/amenidades disponibles en el alojamiento */
        List<ServicioAlojamiento> servicios
) {
    /**
     * Convierte los datos planos del DTO a una instancia de {@link Direccion}.
     * Las coordenadas se calculan luego mediante geocodificación.
     */
    public Direccion toDireccion() {
        return Direccion.builder()
                .ciudad(ciudad)
                .pais(pais)
                .calle(calle)
                .zip(zip)
                .build();
    }

    /**
     * Devuelve siempre una lista no nula de fotos.
     */
    public List<String> getFotosOrEmpty() {
        return fotos != null ? fotos : new ArrayList<>();
    }
}
