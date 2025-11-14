package co.edu.uniquindio.gohost.service.mail;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.model.Usuario;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class MailTemplates {

    public static EmailRequest perfilActualizado(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#9C27B0;color:#ffffff;padding:20px;font-size:18px">Perfil actualizado</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tus datos de perfil han sido actualizados correctamente.</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">© GoHost</td></tr>
                </table></div>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Perfil actualizado")
                .html(html)
                .build();
    }

    public static EmailRequest contraseñaCambiada(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#6a1b9a;color:#ffffff;padding:20px;font-size:18px">Contraseña cambiada</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tu contraseña ha sido actualizada exitosamente.</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Si no fuiste tú, contacta soporte.</td></tr>
                </table></div>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Contraseña cambiada")
                .html(html)
                .build();
    }

    public static EmailRequest contraseñaRestablecida(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#00897b;color:#ffffff;padding:20px;font-size:18px">Contraseña restablecida</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tu contraseña fue restablecida correctamente.</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Si no solicitaste este cambio, contáctanos.</td></tr>
                </table></div>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Contraseña restablecida")
                .html(html)
                .build();
    }

    public static EmailRequest recuperacion(Usuario usuario, String codigo, int minutos) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#ef6c00;color:#ffffff;padding:20px;font-size:18px">Recuperación de contraseña</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 8px 16px">Tu código de verificación para restablecer la contraseña es:</p>
                <div style="font-size:28px;color:#9C27B0;font-weight:bold;letter-spacing:2px">%s</div>
                <p style="margin:16px 0 0">Este código expira en %d minutos.</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Si no solicitaste este cambio, ignora este mensaje.</td></tr>
                </table></div>
                """.formatted(usuario.getNombre(), codigo, minutos);
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Recuperación de contraseña")
                .html(html)
                .build();
    }

    public static EmailRequest confirmacionReservaHuesped(Usuario huesped, Alojamiento alojamiento, Reserva reserva, LocalDate in, LocalDate out) {
        long noches = ChronoUnit.DAYS.between(in, out);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = in.format(fmt);
        String checkOutStr = out.format(fmt);
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#9C27B0;color:#ffffff;padding:20px;font-size:18px">Confirmación de reserva</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 12px">Tu reserva se ha creado correctamente.</p>
                <p style="margin:0 0 8px"><b>Código de reserva:</b></p>
                <div style="font-size:20px;color:#9C27B0;font-weight:bold">%s</div>
                <p style="margin:16px 0 0"><b>Alojamiento:</b> %s</p>
                <p><b>Check-in:</b> %s</p>
                <p><b>Check-out:</b> %s</p>
                <p><b>Noches:</b> %d</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Gracias por reservar con nosotros.</td></tr>
                </table></div>
                """.formatted(
                huesped.getNombre(),
                reserva.getId(),
                alojamiento.getTitulo(),
                checkInStr,
                checkOutStr,
                noches
        );
        return EmailRequest.builder()
                .to(huesped.getEmail())
                .cc(alojamiento.getAnfitrion() != null ? alojamiento.getAnfitrion().getEmail() : null)
                .subject("Confirmación de reserva")
                .html(html)
                .build();
    }

    public static EmailRequest nuevaReservaAnfitrion(Usuario anfitrion, Usuario huesped, Alojamiento alojamiento, Reserva reserva, LocalDate in, LocalDate out, Integer numeroHuespedes) {
        long noches = ChronoUnit.DAYS.between(in, out);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String checkInStr = in.format(fmt);
        String checkOutStr = out.format(fmt);
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#43a047;color:#ffffff;padding:20px;font-size:18px">Nueva reserva</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 12px">Has recibido una nueva reserva en tu alojamiento.</p>
                <p style="margin:0 0 8px"><b>Código de reserva:</b></p>
                <div style="font-size:20px;color:#43a047;font-weight:bold">%s</div>
                <p style="margin:16px 0 0"><b>Alojamiento:</b> %s</p>
                <p><b>Huésped:</b> %s (%s)</p>
                <p><b>Número de huéspedes:</b> %d</p>
                <p><b>Check-in:</b> %s</p>
                <p><b>Check-out:</b> %s</p>
                <p><b>Noches:</b> %d</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Prepárate para recibir a tus huéspedes.</td></tr>
                </table></div>
                """.formatted(
                anfitrion.getNombre(),
                reserva.getId(),
                alojamiento.getTitulo(),
                huesped.getNombre(),
                huesped.getEmail(),
                numeroHuespedes,
                checkInStr,
                checkOutStr,
                noches
        );
        return EmailRequest.builder()
                .to(anfitrion.getEmail())
                .cc(huesped.getEmail())
                .subject("Nueva reserva en tu alojamiento")
                .html(html)
                .build();
    }

    public static EmailRequest reservaConfirmadaHuesped(Reserva reserva) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#43a047;color:#ffffff;padding:20px;font-size:18px">Reserva confirmada</td></tr>
                <tr><td style="padding:24px;color:#333333">Tu reserva %s ha sido confirmada y el pago fue procesado.</td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Gracias por confiar en GoHost.</td></tr>
                </table></div>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getHuesped().getEmail())
                .subject("Reserva confirmada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaConfirmadaAnfitrion(Reserva reserva) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#43a047;color:#ffffff;padding:20px;font-size:18px">Reserva confirmada</td></tr>
                <tr><td style="padding:24px;color:#333333">La reserva %s de tu alojamiento ha sido confirmada.</td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Puedes coordinar la llegada con el huésped.</td></tr>
                </table></div>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getAlojamiento().getAnfitrion().getEmail())
                .cc(reserva.getHuesped().getEmail())
                .subject("Reserva confirmada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaCanceladaHuesped(Reserva reserva) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#c62828;color:#ffffff;padding:20px;font-size:18px">Reserva cancelada</td></tr>
                <tr><td style="padding:24px;color:#333333">Tu reserva %s ha sido cancelada.</td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Si fue un error, contáctanos para ayudarte.</td></tr>
                </table></div>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getHuesped().getEmail())
                .subject("Reserva cancelada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaCanceladaAnfitrion(Reserva reserva) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#c62828;color:#ffffff;padding:20px;font-size:18px">Reserva cancelada</td></tr>
                <tr><td style="padding:24px;color:#333333">La reserva %s de tu alojamiento ha sido cancelada.</td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Te avisaremos ante nuevas reservas.</td></tr>
                </table></div>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getAlojamiento().getAnfitrion().getEmail())
                .cc(reserva.getHuesped().getEmail())
                .subject("Reserva cancelada")
                .html(html)
                .build();
    }
    public static EmailRequest bienvenidaHuesped(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#9C27B0;color:#ffffff;padding:20px;font-size:18px">Bienvenido a GoHost</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tu registro fue exitoso. Ya puedes explorar alojamientos y realizar reservas de forma segura.</p>
                <a href="https://front-gohost-production.up.railway.app" style="display:inline-block;background:#9C27B0;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:8px">Explorar alojamientos</a>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">
                © GoHost • Conectando huéspedes y anfitriones de forma confiable
                </td></tr>
                </table></div>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Bienvenido a GoHost")
                .html(html)
                .build();
    }

    public static EmailRequest bienvenidaAnfitrion(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#43a047;color:#ffffff;padding:20px;font-size:18px">Bienvenido como Anfitrión</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tu registro como anfitrión fue exitoso. Publica tu primer alojamiento y empieza a recibir huéspedes.</p>
                <a href="https://front-gohost-production.up.railway.app" style="display:inline-block;background:#43a047;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:8px">Publicar alojamiento</a>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">
                © GoHost • Impulsa tu hospedaje con herramientas simples y potentes
                </td></tr>
                </table></div>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Bienvenido a GoHost (Anfitrión)")
                .html(html)
                .build();
    }

    public static EmailRequest alojamientoCreadoAnfitrion(Usuario anfitrion, Alojamiento alojamiento) {
        String creado = alojamiento.getFechaCreacion() != null ? alojamiento.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#9C27B0;color:#ffffff;padding:20px;font-size:18px">Alojamiento creado</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 12px">Tu alojamiento <b>%s</b> ha sido creado correctamente.</p>
                <p style="margin:0">Fecha de creación: %s</p>
                </td></tr>
                <tr><td style="padding:16px 24px;color:#888888;font-size:12px">Gracias por confiar en GoHost.</td></tr>
                </table></div>
                """.formatted(anfitrion.getNombre(), alojamiento.getTitulo(), creado);
        return EmailRequest.builder()
                .to(anfitrion.getEmail())
                .subject("Alojamiento creado")
                .html(html)
                .build();
    }
}
