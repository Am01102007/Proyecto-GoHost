package co.edu.uniquindio.gohost.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Utilidad para generar y validar tokens JWT (jjwt 0.11.5).
 * - Lee secret y expiration desde application.yml (jwt.secret, jwt.expiration)
 * - Expone overloads para incluir claims estándar: uid (UUID) y rol
 */
@Component
public class JWTUtils {

    /** Nombres de claims estándar usados en el proyecto */
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_ROL = "rol";

    private final SecretKey key;     // Clave secreta HMAC para firmar tokens
    private final long expirationMs; // Duración del token en milisegundos

    public JWTUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.key = parseKey(secret);
        this.expirationMs = expirationMs;
    }

    /**
     * Convierte la clave secreta a SecretKey válido para HS256.
     * Acepta Base64 o texto plano UTF-8. Exige mínimo 32 bytes.
     */
    private SecretKey parseKey(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length < 32) {
                throw new IllegalArgumentException(
                        "La clave JWT decodificada debe tener al menos 256 bits (32 bytes). Actual: " + decoded.length
                );
            }
            return Keys.hmacShaKeyFor(decoded);
        } catch (IllegalArgumentException base64Fail) {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                throw new IllegalArgumentException(
                        "La clave JWT debe tener al menos 32 caracteres. Actual: " + raw.length +
                                ". Sugerencia: openssl rand -base64 32"
                );
            }
            return Keys.hmacShaKeyFor(raw);
        }
    }

    // ==============================
    // Generación de tokens
    // ==============================

    /**
     * Genera un token con subject y claims arbitrarios.
     * @param subject  normalmente email o username
     * @param claims   mapa de claims extra (puede ser null)
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims != null ? claims : Map.of())
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(now.toEpochMilli() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Genera un token solo con subject (sin claims adicionales). */
    public String generateToken(String subject) {
        return generateToken(subject, Map.of());
    }

    /**
     * Genera un token con claims estándar: uid y rol.
     * @param subject email/username que usará tu UserDetailsService
     * @param uid     UUID del usuario (se serializa como String)
     * @param rol     rol del usuario (HUESPED/ANFITRION/ADMIN)
     */
    public String generateToken(String subject, UUID uid, String rol) {
        return generateToken(subject, buildAuthClaims(uid, rol));
    }

    /**
     * Genera un token con claims estándar + claims adicionales.
     */
    public String generateToken(String subject, UUID uid, String rol, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new java.util.HashMap<>(buildAuthClaims(uid, rol));
        if (extraClaims != null && !extraClaims.isEmpty()) {
            claims.putAll(extraClaims);
        }
        return generateToken(subject, claims);
    }

    /** Crea el mapa de claims estándar del proyecto. */
    public Map<String, Object> buildAuthClaims(UUID uid, String rol) {
        return Map.of(
                CLAIM_UID, uid != null ? uid.toString() : null,
                CLAIM_ROL, rol
        );
    }

    // ==============================
    // Parsing y helpers
    // ==============================

    /** Valida y parsea un token. Lanza JwtException si es inválido/expirado. */
    public Jws<Claims> parseJwt(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    /** Devuelve todos los claims (valida primero la firma y la expiración). */
    public Claims getClaims(String token) {
        return parseJwt(token).getBody();
    }

    /** Obtiene el subject (email/username/id) del token. */
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    /** Verifica si un token es válido (firma y expiración). */
    public boolean isValid(String token) {
        try {
            parseJwt(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}