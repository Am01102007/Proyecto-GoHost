package co.edu.uniquindio.gohost.exception;

/**
 * Excepción personalizada para errores en el proceso de restablecimiento de contraseñas.
 * Se lanza cuando hay problemas con tokens de recuperación, códigos expirados, etc.
 */
public class PasswordResetException extends RuntimeException {

    public PasswordResetException(String message) {
        super(message);
    }

    public PasswordResetException(String message, Throwable cause) {
        super(message, cause);
    }
}