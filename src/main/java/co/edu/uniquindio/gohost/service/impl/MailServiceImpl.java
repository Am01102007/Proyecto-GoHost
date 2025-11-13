package co.edu.uniquindio.gohost.service.impl;


import co.edu.uniquindio.gohost.service.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * 📬 MailServiceImpl — Implementación concreta de {@link MailService}
 * ============================================================================
 *
 * Esta clase usa la librería <b>Simple Java Mail</b> para enviar correos electrónicos
 * mediante un servidor SMTP configurado en `application.yml`.
 *
 * Ventajas:
 *  - API más simple y robusta que JavaMailSender.
 *  - Soporte automático para TLS/STARTTLS.
 *  - Registro detallado de envío con `withDebugLogging(true)`.
 *
 * Flujo general:
 *  1. Construye el correo con {@link EmailBuilder}.
 *  2. Crea un cliente SMTP con {@link MailerBuilder}.
 *  3. Envía el mensaje usando {@link Mailer#sendMail(Email)}.
 *
 * Configuración leída desde:
 *  - `spring.mail.username`
 *  - `spring.mail.password`
 *  - `spring.mail.host`
 *  - `spring.mail.port`
 *
 * Ejemplo de uso:
 * <pre>{@code
 * mailService.sendMail("usuario@correo.com",
 *                      "Bienvenido a GoHost",
 *                      "<h1>¡Hola!</h1><p>Tu registro fue exitoso.</p>");
 * }</pre>
 */
@Service
public class MailServiceImpl implements MailService {
    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${mail.enabled:false}")
    private boolean enabled;
    /**
     * Envía un correo electrónico HTML utilizando Simple Java Mail.
     *
     * @param to      destinatario
     * @param subject asunto
     * @param html    cuerpo HTML
     * @throws Exception si ocurre un error al construir o enviar el correo
     */
    @Override
    public void sendMail(String to, String subject, String html) throws Exception {
        // Si el envío de correo está deshabilitado, hacer no-op para delegar al frontend (EmailJS)
        if (!enabled) {
            log.info("Mail deshabilitado (mail.enabled=false). No se enviará correo a {} con asunto '{}'", to, subject);
            return;
        }

        Email email = EmailBuilder.startingBlank()
                .from(username)
                .to(to)
                .withSubject(subject)
                .withHTMLText(html)
                .buildEmail();

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
        }
    }

}
