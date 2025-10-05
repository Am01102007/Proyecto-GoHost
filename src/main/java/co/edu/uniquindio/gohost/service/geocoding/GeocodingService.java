package co.edu.uniquindio.gohost.service.geocoding;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * ============================================================================
 * GeocodingService — Servicio para obtener coordenadas geográficas (lat/lon)
 * ============================================================================
 *
 * Responsabilidades:
 *  - Convertir direcciones en coordenadas (geocodificación directa).
 *  - Permitir diferentes proveedores (Nominatim, OpenCage, Geoapify, etc.)
 *    según la configuración en application.yml.
 *
 * Buenas prácticas:
 *  - Retornar null si no se encuentra la ubicación o si ocurre un error controlado.
 *  - No lanzar excepciones checked (solo Runtime o propias si es necesario).
 *  - Preservar UTF-8 en nombres de ciudades/direcciones.
 */
public interface GeocodingService {

    /**
     * Obtiene las coordenadas geográficas (latitud, longitud) para una dirección dada.
     *
     * @param direccion Dirección completa o aproximada (ejemplo: "Cra 10 #20-30, Armenia, Colombia")
     * @param ciudad    Ciudad (opcional, mejora la precisión)
     * @param pais      País (opcional)
     * @return Coordenadas o null si no se encuentra la ubicación.
     */
    @Nullable
    Coordenadas obtenerCoordenadas(@NonNull String direccion,
                                   @Nullable String ciudad,
                                   @Nullable String pais);

    /**
     * DTO simple para representar coordenadas geográficas.
     */
    record Coordenadas(double latitud, double longitud) {}
}

