package co.edu.uniquindio.gohost.security;

import co.edu.uniquindio.gohost.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro JWT:
 *  - Extrae y valida el token (Authorization: Bearer ...).
 *  - Carga el usuario y pobla el SecurityContext si aplica.
 *  - Expone "usuarioId" (UUID) y "rol" como atributos del request para los controladores.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Rutas públicas que no requieren autenticación
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth", "/swagger-ui", "/v3/api-docs", "/openapi.yaml", "/h2-console", "/api-docs", "/health", "/actuator/health", "/error"
    );

    private final JWTUtils jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getServletPath();
        // Permitir preflight CORS
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // Excluir prefijos públicos
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String token = resolveToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1) Validar y parsear JWT
                Jws<Claims> jws = jwtUtil.parseJwt(token);
                Claims claims = jws.getBody();

                // 2) Extraer identificadores
                String subject = claims.getSubject();                 // puede ser username/email o UUID
                String uidStr  = claims.get("uid", String.class);     // preferido
                String rol     = claims.get("rol", String.class);     // preferido
                if (!StringUtils.hasText(rol)) {
                    rol = claims.get("role", String.class);           // respaldo si el emisor usa 'role'
                }

                // 3) Exponer atributos al request (tolerante)
                UUID usuarioId = extractUuid(uidStr);
                if (usuarioId == null) {
                    usuarioId = extractUuid(subject); // si sub es un UUID válido
                }
                if (usuarioId != null) {
                    request.setAttribute("usuarioId", usuarioId);
                }
                if (StringUtils.hasText(rol)) {
                    request.setAttribute("rol", rol);
                }

                // 4) Autenticación en el contexto de Spring Security
                if (StringUtils.hasText(subject)) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                    } catch (Exception ex) {
                        log.warn("No se pudo cargar usuario '{}' desde UserDetailsService: {}", subject, ex.getMessage());
                    }
                }

            } catch (JwtException ex) {
                log.warn("Token JWT inválido: {}", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                log.warn("Token JWT mal formado: {}", ex.getMessage());
            } catch (Exception ex) {
                log.error("Error inesperado al procesar JWT", ex);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extrae el token del header Authorization con formato Bearer.
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length()).trim();
    }

    /**
     * Intenta parsear un UUID desde una cadena (retorna null si no es válido).
     */
    private UUID extractUuid(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}