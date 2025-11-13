package co.edu.uniquindio.gohost.dto.usuarioDtos;

/**
 * Payload informativo para el flujo de recuperación de contraseña.
 * Nota: en producción el backend envía el correo; este payload puede
 * usarse para mostrar mensajes al usuario (evitar exponer el código en UI).
 */
public record ResetPasswordPayloadDTO(
        String email,
        String nombre,
        String codigo,
        int expiraEnMinutos
) {}
