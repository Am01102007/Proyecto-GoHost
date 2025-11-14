package co.edu.uniquindio.gohost.service.mail;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.model.Usuario;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MailTemplates {

    public static EmailRequest perfilActualizado(Usuario usuario) {
        String html = """
                <h2>Perfil actualizado</h2>
                <p>Hola %s,</p>
                <p>Tus datos de perfil han sido actualizados correctamente.</p>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Perfil actualizado")
                .html(html)
                .build();
    }

    public static EmailRequest contraseñaCambiada(Usuario usuario) {
        String html = """
                <h2>Contraseña cambiada</h2>
                <p>Hola %s,</p>
                <p>Tu contraseña ha sido actualizada exitosamente.</p>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Contraseña cambiada")
                .html(html)
                .build();
    }

    public static EmailRequest contraseñaRestablecida(Usuario usuario) {
        String html = """
                <h2>Contraseña restablecida</h2>
                <p>Hola %s,</p>
                <p>Tu contraseña fue restablecida correctamente.</p>
                """.formatted(usuario.getNombre());
        return EmailRequest.builder()
                .to(usuario.getEmail())
                .subject("Contraseña restablecida")
                .html(html)
                .build();
    }

    public static EmailRequest recuperacion(Usuario usuario, String codigo, int minutos) {
        String html = """
                <h2>Recuperación de contraseña</h2>
                <p>Hola %s,</p>
                <p>Tu código de verificación para restablecer la contraseña es:</p>
                <h1 style=\"color:#007bff;\">%s</h1>
                <p>Este código expira en %d minutos.</p>
                <p>Si no solicitaste este cambio, ignora este mensaje.</p>
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
                <h2>Confirmación de reserva</h2>
                <p>Hola %s,</p>
                <p>Tu reserva se ha creado correctamente.</p>
                <p><b>Código de reserva:</b></p>
                <h1 style=\"color:#007BFF;\">%s</h1>
                <p><b>Alojamiento:</b> %s</p>
                <p><b>Check-in:</b> %s</p>
                <p><b>Check-out:</b> %s</p>
                <p><b>Noches:</b> %d</p>
                <br/>
                <p>Gracias por reservar con nosotros. Si tienes dudas, responde este correo.</p>
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
                <h2>Nueva reserva en tu alojamiento</h2>
                <p>Hola %s,</p>
                <p>Has recibido una nueva reserva en tu alojamiento.</p>
                <p><b>Código de reserva:</b></p>
                <h1 style=\"color:#28a745;\">%s</h1>
                <p><b>Alojamiento:</b> %s</p>
                <p><b>Huésped:</b> %s (%s)</p>
                <p><b>Número de huéspedes:</b> %d</p>
                <p><b>Check-in:</b> %s</p>
                <p><b>Check-out:</b> %s</p>
                <p><b>Noches:</b> %d</p>
                <br/>
                <p>Puedes contactar al huésped respondiendo a este correo o a través de la plataforma.</p>
                <p>¡Prepárate para recibir a tus huéspedes!</p>
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
                .subject("Nueva reserva en tu alojamiento")
                .html(html)
                .build();
    }

    public static EmailRequest reservaConfirmadaHuesped(Reserva reserva) {
        String html = """
                <h2>Reserva confirmada</h2>
                <p>Tu reserva %s ha sido confirmada y el pago fue procesado.</p>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getHuesped().getEmail())
                .subject("Reserva confirmada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaConfirmadaAnfitrion(Reserva reserva) {
        String html = """
                <h2>Reserva confirmada</h2>
                <p>La reserva %s de tu alojamiento ha sido confirmada.</p>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getAlojamiento().getAnfitrion().getEmail())
                .subject("Reserva confirmada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaCanceladaHuesped(Reserva reserva) {
        String html = """
                <h2>Reserva cancelada</h2>
                <p>Tu reserva %s ha sido cancelada.</p>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getHuesped().getEmail())
                .subject("Reserva cancelada")
                .html(html)
                .build();
    }

    public static EmailRequest reservaCanceladaAnfitrion(Reserva reserva) {
        String html = """
                <h2>Reserva cancelada</h2>
                <p>La reserva %s de tu alojamiento ha sido cancelada.</p>
                """.formatted(reserva.getId());
        return EmailRequest.builder()
                .to(reserva.getAlojamiento().getAnfitrion().getEmail())
                .subject("Reserva cancelada")
                .html(html)
                .build();
    }
    public static EmailRequest bienvenidaHuesped(Usuario usuario) {
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f7f7f9;padding:24px">
                <table role="presentation" style="max-width:600px;margin:auto;background:#ffffff;border-radius:12px;overflow:hidden">
                <tr><td style="background:#1e88e5;color:#ffffff;padding:20px;font-size:18px">Bienvenido a GoHost</td></tr>
                <tr><td style="padding:24px;color:#333333">
                <p style="margin:0 0 12px">Hola %s,</p>
                <p style="margin:0 0 16px">Tu registro fue exitoso. Ya puedes explorar alojamientos y realizar reservas de forma segura.</p>
                <a href="https://front-gohost-production.up.railway.app" style="display:inline-block;background:#1e88e5;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:8px">Explorar alojamientos</a>
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
}
