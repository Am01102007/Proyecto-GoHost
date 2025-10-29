package co.edu.uniquindio.gohost.exception;

/**
 * Excepción personalizada para errores del servicio de correo electrónico.
 * Se lanza cuando hay problemas al enviar correos de recuperación, notificaciones, etc.
 */
public class MailServiceException extends RuntimeException {

    public MailServiceException(String message) {
        super(message);
    }

    public MailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}