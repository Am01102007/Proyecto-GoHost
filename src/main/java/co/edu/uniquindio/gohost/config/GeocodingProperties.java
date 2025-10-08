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
 * Mapea application.yml con prefijo "geocoding".
 * Incluye proveedor/baseUrl/apiKey (tuyos) + banderas y parámetros de robustez.
 *
 * Ejemplo en application.yml:
 *
 * geocoding:
 *   enabled: true
 *   provider: nominatim
 *   base-url: "https://nominatim.openstreetmap.org/search"
 *   api-key: ""
 *   user-agent: "GoHost/1.0 (contacto: tu-email@ejemplo.com)"
 *   referer: "https://gohost.local"
 *   language: "es"
 *   limit: 1
 *   timeout-ms: 4000
 *   retries: 1
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "geocoding")
public class GeocodingProperties {

    /** Habilitar/deshabilitar geocodificación (útil en pruebas) */
    private boolean enabled = true;

    /** Proveedor (nominatim, opencage, geoapify, etc.) */
    private String provider = "nominatim";

    /** URL base del endpoint del servicio */
    private String baseUrl = "https://nominatim.openstreetmap.org/search";

    /** Clave de API si el proveedor la requiere */
    private String apiKey = "";

    /** User-Agent requerido por TOS de algunos proveedores (Nominatim) */
    private String userAgent = "GoHost/1.0 (contacto: admin@example.com)";

    /** Referer opcional (cortesía / TOS) */
    private String referer = "https://gohost.local";

    /** Idioma preferido en resultados */
    private String language = "es";

    /** Límite de resultados (1 = tomamos el primero) */
    private int limit = 1;

    /** Timeout en milisegundos de la request HTTP */
    private int timeoutMs = 4000;

    /** Reintentos al fallar (>=0). Con 1 habrá 2 intentos en total */
    private int retries = 1;
}