package co.edu.uniquindio.gohost.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Componente utilitario para obtener el usuario autenticado de forma estandarizada.
 * Centraliza la lógica de extracción del ID del usuario desde el request.
 */
@Component
public class AuthenticationHelper {

    /**
     * Obtiene el ID del usuario autenticado desde el request.
     * El filtro JWT debe haber establecido el atributo "usuarioId" previamente.
     * 
     * @param request HttpServletRequest que contiene el atributo usuarioId
     * @return UUID del usuario autenticado
     * @throws IllegalStateException si no se encuentra el usuario autenticado
     */
    public UUID getAuthenticatedUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("usuarioId");
        
        if (attr instanceof UUID uuid) {
            return uuid;
        }
        
        if (attr instanceof String s) {
            try { 
                return UUID.fromString(s); 
            } catch (IllegalArgumentException ignored) {
                // Continúa al error general
            }
        }
        
        // Si el filtro no puso el atributo, señalamos 401 de forma clara.
        throw new IllegalStateException("No se encontró usuario autenticado (usuarioId).");
    }
}