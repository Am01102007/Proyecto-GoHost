package co.edu.uniquindio.gohost.service.mail;

import org.springframework.lang.NonNull;

/**
 * ============================================================================
 * 📧 MailService — Contrato para el envío de correos transaccionales
 * ============================================================================
 *
 * Responsabilidades:
 *  - Enviar correos electrónicos en formato HTML (UTF-8).
 *  - Centralizar la lógica de notificaciones por correo dentro del sistema GoHost.
 *
 * Características:
 *  - Soporta HTML con codificación UTF-8.
 *  - Expone una API sencilla orientada a casos comunes de negocio.
 *
 * Reglas generales:
 *  - Los métodos pueden lanzar excepciones controladas de tipo {@link Exception}
 *    si ocurre un error durante el envío (autenticación, red, formato, etc.).
 *  - Los parámetros obligatorios deben ser validados antes del envío.
 *  - La configuración SMTP se obtiene desde el archivo `application.yml`:
 *
 *        spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password
 *
 * Buenas prácticas:
 *  - No incluir información sensible (contraseñas o tokens) en los logs.
 *  - Mantener la codificación UTF-8 en el asunto y cuerpo del correo.
 */
public interface MailService {

    /**
     * Envío simple de correo en HTML (UTF-8).
     * Usar este método para la mayoría de notificaciones (recuperación de contraseña, confirmaciones, etc.).
     *
     * @param to      destinatario principal (obligatorio)
     * @param subject asunto del correo (obligatorio)
     * @param html    cuerpo en HTML (obligatorio)
     */
    void sendMail(@NonNull String to,
                @NonNull String subject,
                @NonNull String html) throws Exception;

    void send(EmailRequest request) throws Exception;

    void sendAsync(EmailRequest request);

}
