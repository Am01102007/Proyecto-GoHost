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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro que se ejecuta una vez por request.
 * Se encarga de:
 *  - Extraer el token JWT del header Authorization.
 *  - Validar el token y obtener el usuario.
 *  - Poblar el contexto de seguridad con la autenticación correspondiente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Rutas públicas que no requieren autenticación
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth", "/swagger-ui", "/v3/api-docs", "/openapi.yaml", "/h2-console"
    );

    private final JWTUtils jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Excluye del filtro las rutas públicas
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getServletPath();
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /**
     * Filtra cada request para extraer y validar el token JWT.
     * Si algo falla (token inválido, usuario no existe, etc), simplemente
     * no autentica y deja pasar el request. El AuthenticationEntryPoint
     * se encargará de rechazarlo si intenta acceder a recursos protegidos.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Paso 1: Extraer token del header
        final String token = resolveToken(request);

        // Paso 2: Procesar solo si hay token y no hay autenticación previa
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Paso 3: Validar token y extraer username
                // parseJwt lanza JwtException si el token es inválido, expirado, etc
                Jws<Claims> jws = jwtUtil.parseJwt(token);
                String username = jws.getBody().getSubject();

                // Paso 4: Verificar que el username existe
                if (StringUtils.hasText(username)) {
                    try {
                        // Paso 5: Cargar usuario desde la BD
                        // Puede lanzar UsernameNotFoundException si el usuario fue eliminado
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Paso 6: Crear objeto de autenticación
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // Paso 7: Agregar detalles del request (IP, session, etc)
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Paso 8: Guardar autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(auth);

                    } catch (Exception ex) {
                        // Usuario no encontrado o error al cargar desde BD
                        // Log y continuar sin autenticar (el request será rechazado después)
                        log.warn("No se pudo cargar usuario '{}' del token: {}",
                                username, ex.getMessage());
                    }
                }

            } catch (JwtException ex) {
                // Token inválido, expirado, firma incorrecta, etc
                log.warn("Token JWT inválido: {}", ex.getMessage());

            } catch (IllegalArgumentException ex) {
                // Token mal formado o null
                log.warn("Token JWT mal formado: {}", ex.getMessage());

            } catch (Exception ex) {
                // Cualquier otro error inesperado
                log.error("Error inesperado al procesar JWT", ex);
            }
        }

        // Paso 9: Continuar con la cadena de filtros
        // Si no se autenticó, el AuthenticationEntryPoint rechazará requests a endpoints protegidos
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

}
