package co.edu.uniquindio.gohost.service.mail;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailSendException;
import org.springframework.core.io.InputStreamSource;

import java.util.Collection;
import java.util.Map;

/**
 * ============================================================================
 * MailService — Contrato para envío de correos transaccionales
 * ============================================================================
 *
 * Responsabilidades:
 *  - Enviar correos en HTML (UTF-8) y texto plano.
 *  - Soportar CC/BCC y adjuntos (opcional).
 *  - Exponer una API sencilla para casos comunes de negocio.
 *
 * Reglas generales:
 *  - Los métodos NO deben lanzar excepciones checked. Cualquier error se envuelve
 *    en {@link MailSendException}.
 *  - La implementación debe leer la configuración de correo desde application.yml:
 *
 *      spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password, etc.
 *
 * Buenas prácticas:
 *  - No loggear contenido sensible (tokens, contraseñas). En logs, incluir solo metadatos.
 *  - Preservar codificación UTF-8 para sujetos y cuerpos.
 */
public interface MailService {

    /**
     * Envío simple de correo en HTML (UTF-8).
     * Usar este método para la mayoría de notificaciones (recuperación de contraseña, confirmaciones, etc.).
     *
     * @param to      destinatario principal (obligatorio)
     * @param subject asunto del correo (obligatorio)
     * @param html    cuerpo en HTML (obligatorio)
     * @throws MailSendException si ocurre un error al enviar
     */
    void enviar(@NonNull String to,
                @NonNull String subject,
                @NonNull String html) throws MailSendException;

    /**
     * Envío de correo en texto plano (sin HTML).
     *
     * @param to      destinatario principal (obligatorio)
     * @param subject asunto del correo (obligatorio)
     * @param text    cuerpo en texto plano (obligatorio)
     * @throws MailSendException si ocurre un error al enviar
     */
    void enviarTexto(@NonNull String to,
                     @NonNull String subject,
                     @NonNull String text) throws MailSendException;

    /**
     * Envío avanzado con CC/BCC y adjuntos. El cuerpo puede ser HTML o texto plano según el flag.
     *
     * @param to            destinatario principal (obligatorio)
     * @param cc            colección de CC (puede ser null o vacía)
     * @param bcc           colección de BCC (puede ser null o vacía)
     * @param subject       asunto (obligatorio)
     * @param body          cuerpo (obligatorio)
     * @param isHtml        true para HTML, false para texto plano
     * @param attachments   mapa nombreArchivo -> contenido (InputStreamSource),
     *                      puede ser null o vacío. Ej: ByteArrayResource, FileSystemResource.
     * @throws MailSendException si ocurre un error al enviar
     */
    void enviarAvanzado(@NonNull String to,
                        @Nullable Collection<String> cc,
                        @Nullable Collection<String> bcc,
                        @NonNull String subject,
                        @NonNull String body,
                        boolean isHtml,
                        @Nullable Map<String, InputStreamSource> attachments) throws MailSendException;
}
