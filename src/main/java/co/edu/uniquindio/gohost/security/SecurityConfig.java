package co.edu.uniquindio.gohost.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad profesional autocontenida.
 * No requiere clases adicionales de configuración.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    /**
     * Configuración principal de seguridad para la API.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurando seguridad de la aplicación...");

        return http
                // Deshabilitar CSRF para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                // Configuración CORS
                .cors(Customizer.withDefaults())

                // Sesiones stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Autorización de endpoints
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers(
                                "/api/auth/**",           // Autenticación
                                "/swagger-ui/**",         // Swagger UI
                                "/api/usuarios/password/reset",// Ruta pública para el reset de contraseñas
                                "/api/usuarios/password/confirm",
                                "/v3/api-docs/**",        // OpenAPI docs
                                "/openapi.yaml",          // Especificación OpenAPI
                                "/api-docs/**",           // Documentación
                                "/health",                // Health check
                                "/actuator/health",       // Health check de Spring Boot
                                "/error"                  // Página de error
                        ).permitAll()

                        // Endpoints de Actuator (solo health público)
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Manejo de excepciones
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Headers de seguridad
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                        )
                )

                // Filtro JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }


    /**
     * Password encoder seguro con BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configurando BCrypt password encoder con factor de costo 12");
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationManager para autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}