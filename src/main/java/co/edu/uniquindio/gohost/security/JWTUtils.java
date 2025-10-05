package co.edu.uniquindio.gohost.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Utilidad para generar y validar tokens JWT usando jjwt 0.11.5.
 * La clave secreta y el tiempo de expiración se configuran en application.yml.
 */
@Component
public class JWTUtils {

    private final SecretKey key;     // Clave secreta HMAC para firmar los tokens
    private final long expirationMs; // Duración del token en milisegundos

    /**
     * Constructor: inyecta los valores desde application.yml
     *  jwt.secret = clave (puede ser texto plano o Base64)
     *  jwt.expiration = tiempo de expiración en ms
     */
    public JWTUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.key = parseKey(secret);
        this.expirationMs = expirationMs;
    }

    /**
     * Convierte la clave secreta a un objeto SecretKey válido para HS256.
     * @param secret la clave desde application.yml (Base64 o texto plano)
     * @return SecretKey válida para firmar/verificar tokens JWT
     * @throws IllegalArgumentException si la clave es muy corta (< 32 bytes)
     */
    private SecretKey parseKey(String secret) {
        try {
            // Intento 1: decodificar como Base64
            byte[] decoded = Decoders.BASE64.decode(secret);

            // Validar longitud mínima
            if (decoded.length < 32) {
                throw new IllegalArgumentException(
                        "La clave JWT debe tener al menos 256 bits (32 bytes). " +
                                "Clave actual: " + decoded.length + " bytes"
                );
            }

            return Keys.hmacShaKeyFor(decoded);

        } catch (IllegalArgumentException e) {
            // Intento 2: si no es Base64 válido, usar como texto plano UTF-8
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);

            // Validar longitud mínima
            if (raw.length < 32) {
                throw new IllegalArgumentException(
                        "La clave JWT debe tener al menos 32 caracteres. " +
                                "Clave actual: " + raw.length + " caracteres. " +
                                "Genera una clave segura con: openssl rand -base64 32"
                );
            }

            return Keys.hmacShaKeyFor(raw);
        }
    }

    /**
     * Genera un token JWT con subject y claims opcionales.
     */
    public String generateToken(String id, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims != null ? claims : Map.of())
                .setSubject(id)
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(now.toEpochMilli() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String id) {
        return generateToken(id, Map.of());
    }

    /**
     * Valida y parsea un token.
     */
    public Jws<Claims> parseJwt(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    /**
     * Obtiene el "subject" (username o id) de un token.
     */
    public String getUsername(String token) {
        return parseJwt(token).getBody().getSubject();
    }

    /**
     * Verifica si un token es válido.
     */
    public boolean isValid(String token) {
        try {
            parseJwt(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
