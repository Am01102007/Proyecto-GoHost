package co.edu.uniquindio.gohost.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * =============================================================================
 * CorsConfig — Configuración global de CORS para el backend GoHost
 * =============================================================================
 *
 * Esta clase usa las propiedades cargadas desde {@link CorsProperties} para
 * registrar las reglas de CORS (Cross-Origin Resource Sharing).
 *
 * ¿Qué hace?
 *  - Permite peticiones desde dominios definidos en application.yml.
 *  - Habilita métodos HTTP específicos.
 *  - Permite cabeceras personalizadas (Authorization, Content-Type, etc.).
 *  - Define si se permiten cookies o encabezados con credenciales.
 *
 * Es especialmente útil para integrarse con un frontend Angular o React
 * corriendo en http://localhost:4200 o en dominios de producción.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    /**
     * Registra las reglas globales de CORS usando la configuración externa.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configurando CORS global con orígenes permitidos: {}", corsProperties.getAllowedOrigins());

        registry.addMapping("/**")
                .allowedOrigins(
                        corsProperties.getAllowedOrigins().toArray(new String[0])
                )
                .allowedMethods(
                        corsProperties.getAllowedMethods().toArray(new String[0])
                )
                .allowedHeaders(
                        corsProperties.getAllowedHeaders().toArray(new String[0])
                )
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}