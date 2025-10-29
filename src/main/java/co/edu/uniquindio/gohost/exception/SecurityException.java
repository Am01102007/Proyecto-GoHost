package co.edu.uniquindio.gohost.exception;

/**
 * Excepción personalizada para errores de seguridad.
 * Se lanza cuando un usuario intenta acceder a recursos que no le pertenecen
 * o cuando no tiene los permisos necesarios para realizar una operación.
 */
public class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}