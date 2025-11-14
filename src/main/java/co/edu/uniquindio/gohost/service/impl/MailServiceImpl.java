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
import org.springframework.scheduling.annotation.Async;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

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

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.host}")
    private String host;

    @Value("${mail.enabled:false}")
    private boolean enabled;

    @Value("${mail.from:${mail.username}}")
    private String from;

    @Value("${mail.tls:true}")
    private boolean tls;

    @Value("${mail.ssl:false}")
    private boolean ssl;

    @Value("${mail.provider:backend}")
    private String provider;

    @Value("${mail.api-url:https://api.elasticemail.com/v2/email/send}")
    private String apiUrl;

    @Value("${mail.transactional:true}")
    private boolean transactional;
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
        if (!enabled) {
            log.info("Mail deshabilitado. No se enviará correo a {} con asunto '{}'", to, subject);
            return;
        }

        if (provider != null && provider.equalsIgnoreCase("api")) {
            sendViaElasticApi(from, to, subject, html);
            return;
        }
        if (provider != null && provider.equalsIgnoreCase("mailgun")) {
            sendViaMailgunApi(from, to, subject, html);
            return;
        }

        Email email = EmailBuilder.startingBlank()
                .from(from)
                .to(to)
                .withSubject(subject)
                .withHTMLText(html)
                .buildEmail();

        TransportStrategy strategy = ssl ? TransportStrategy.SMTPS : (tls ? TransportStrategy.SMTP_TLS : TransportStrategy.SMTP);

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(strategy)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
        }
    }

    @Override
    public void send(co.edu.uniquindio.gohost.service.mail.EmailRequest request) throws Exception {
        if (request == null) {
            return;
        }
        String to = request.getTo();
        String subject = request.getSubject();
        String html = request.getHtml();
        String fromOverride = request.getFrom();

        if (!enabled || (provider != null && !provider.equalsIgnoreCase("backend"))) {
            log.info("Mail deshabilitado o proveedor no backend. No se enviará correo a {} con asunto '{}'", to, subject);
            return;
        }

        Email email = EmailBuilder.startingBlank()
                .from(fromOverride != null && !fromOverride.isBlank() ? fromOverride : from)
                .to(to)
                .withSubject(subject)
                .withHTMLText(html)
                .buildEmail();

        TransportStrategy strategy = ssl ? TransportStrategy.SMTPS : (tls ? TransportStrategy.SMTP_TLS : TransportStrategy.SMTP);

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(strategy)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
        }
    }

    @Async("mailExecutor")
    @Override
    public void sendAsync(co.edu.uniquindio.gohost.service.mail.EmailRequest request) {
        try {
            send(request);
        } catch (Exception e) {
            log.error("Fallo enviando correo async a {}: {}", request != null ? request.getTo() : null, e.getMessage());
        }
    }

    private void sendViaElasticApi(String fromAddr, String toAddr, String subject, String html) {
        try {
            RestTemplate rt = new RestTemplate();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("apikey", password);
            params.add("subject", subject);
            params.add("from", fromAddr);
            params.add("to", toAddr);
            params.add("bodyHtml", html);
            params.add("isTransactional", Boolean.toString(transactional));
            String res = rt.postForObject(apiUrl, new org.springframework.http.HttpEntity<>(params, new org.springframework.http.HttpHeaders() {{ setContentType(MediaType.APPLICATION_FORM_URLENCODED); }}), String.class);
            log.info("ElasticEmail API respuesta: {}", res);
        } catch (Exception ex) {
            log.error("Fallo ElasticEmail API: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private void sendViaMailgunApi(String fromAddr, String toAddr, String subject, String html) {
        try {
            HttpResponse<JsonNode> res = Unirest.post(apiUrl)
                    .basicAuth("api", password)
                    .field("from", fromAddr)
                    .field("to", toAddr)
                    .field("subject", subject)
                    .field("html", html)
                    .asJson();
            String body = res.getBody() != null ? res.getBody().toString() : String.valueOf(res.getStatus());
            log.info("Mailgun API respuesta: {}", body);
        } catch (UnirestException ex) {
            log.error("Fallo Mailgun API: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}
