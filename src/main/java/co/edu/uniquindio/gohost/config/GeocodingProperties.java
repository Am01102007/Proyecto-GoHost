package co.edu.uniquindio.gohost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * =============================================================================
 * GeocodingProperties — Configuración para el servicio de geocodificación
 * =============================================================================
 *
 * Esta clase mapea automáticamente las propiedades definidas en application.yml
 * bajo la clave `geocoding.*`.
 *
 * Ejemplo en application.yml:
 *
 * geocoding:
 *   provider: nominatim
 *   base-url: "https://nominatim.openstreetmap.org/search"
 *   api-key: ""
 *
 * Uso:
 *   - Inyecta esta clase donde necesites la configuración:
 *       @Autowired
 *       private GeocodingProperties geoProps;
 *
 *   - Permite cambiar el proveedor sin modificar el código fuente.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "geocoding")
public class GeocodingProperties {

    /**
     * Proveedor del servicio de geocodificación (nominatim, opencage, geoapify...).
     * Por defecto: nominatim.
     */
    private String provider;

    /**
     * URL base del endpoint del servicio.
     */
    private String baseUrl;

    /**
     * Clave de API, si el proveedor la requiere.
     * Ejemplo: para OpenCage o Geoapify.
     */
    private String apiKey;
}
