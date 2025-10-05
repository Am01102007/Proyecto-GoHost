package co.edu.uniquindio.gohost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * =============================================================================
 * CorsProperties — Configuración de CORS desde application.yml
 * =============================================================================
 *
 * Esta clase mapea las propiedades definidas bajo `cors.*` en el archivo
 * application.yml, y permite configurar orígenes, métodos y cabeceras
 * permitidas para el frontend (por ejemplo Angular en http://localhost:4200).
 *
 * Ejemplo en application.yml:
 *
 * cors:
 *   allowed-origins:
 *     - http://localhost:4200
 *   allowed-methods:
 *     - GET
 *     - POST
 *   allowed-headers:
 *     - Authorization
 *     - Content-Type
 *   allow-credentials: true
 *   max-age: 3600
 *
 * Posteriormente, estas propiedades se usan en una clase de configuración
 * tipo WebMvcConfigurer para registrar el CORS global.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /** Lista de orígenes permitidos (por ejemplo: http://localhost:4200). */
    private List<String> allowedOrigins;

    /** Métodos HTTP permitidos (GET, POST, PUT, DELETE, etc.). */
    private List<String> allowedMethods;

    /** Cabeceras HTTP permitidas. */
    private List<String> allowedHeaders;

    /** Indica si las credenciales (cookies, Authorization) están permitidas. */
    private boolean allowCredentials;

    /** Tiempo máximo que el navegador puede cachear la respuesta preflight (en segundos). */
    private long maxAge;
}