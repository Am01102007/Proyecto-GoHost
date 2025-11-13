package co.edu.uniquindio.gohost.dto.usuarioDtos;

/**
 * Payload para que el frontend (EmailJS) envíe el correo de recuperación.
 */
public record ResetPasswordPayloadDTO(
        String email,
        String nombre,
        String codigo,
        int expiraEnMinutos
) {}

