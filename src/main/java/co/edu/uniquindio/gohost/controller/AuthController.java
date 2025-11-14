package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.authDtos.LoginDTO;
import co.edu.uniquindio.gohost.dto.authDtos.RegistroDTO;
import co.edu.uniquindio.gohost.dto.authDtos.TokenDTO;
import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.security.JWTUtils;   // Utilidad JWT correcta
import co.edu.uniquindio.gohost.service.UsuarioService;
import co.edu.uniquindio.gohost.service.mail.EmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación y registro (orientado a producción).
 * Endpoints bajo "/api/auth" para login y registro seguro.
 * Consideraciones de producción: rate limiting en login, auditoría de intentos,
 * y JWT con secreto fuerte definido por entorno.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarios;  // Servicio de dominio para usuarios
    private final JWTUtils jwtUtils;        // Utilidad para generar/validar JWT
    private final co.edu.uniquindio.gohost.service.mail.MailService mailService;

    /**
     * Registra un nuevo usuario con rol HUESPED por defecto.
     * La contraseña se encripta en UsuarioServiceImpl antes de persistir.
     *
     * @param dto datos de registro
     * @return usuario creado (HTTP 201)
     */
    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegistroDTO dto) {
        // Construcción del usuario (el password se encripta en el service)
        var u = Usuario.builder()
                .email(dto.email())
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .tipoDocumento(dto.tipoDocumento())
                .numeroDocumento(dto.numeroDocumento())
                .fechaNacimiento(dto.fechaNacimiento())
                .telefono(dto.telefono())
                .ciudad(dto.ciudad())
                .pais(dto.pais())
                .password(dto.password())
                .rol(Rol.HUESPED)
                .activo(true)
                .build();
        var created = usuarios.crear(u);
        try {
            String html = """
                <h2>Bienvenido a GoHost</h2>
                <p>Hola %s,</p>
                <p>Tu registro fue exitoso. Ya puedes iniciar sesión y reservar.</p>
                """.formatted(created.getNombre());
            mailService.sendAsync(EmailRequest.builder().to(created.getEmail()).subject("Bienvenido a GoHost").html(html).build());
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    /**
     * Registra un nuevo usuario con rol ANFITRION por defecto.
     * La contraseña se encripta en UsuarioServiceImpl antes de persistir.
     *
     * @param dto datos de registro
     * @return usuario creado (HTTP 201)
     */
    @PostMapping("/register/anfitrion")
    public ResponseEntity<Usuario> registerHost(@Valid @RequestBody RegistroDTO dto) {
        // Construcción del usuario (el password se encripta en el service)
        var u = Usuario.builder()
                .email(dto.email())
                .nombre(dto.nombre())
                .apellidos(dto.apellidos())
                .tipoDocumento(dto.tipoDocumento())
                .numeroDocumento(dto.numeroDocumento())
                .fechaNacimiento(dto.fechaNacimiento())
                .telefono(dto.telefono())
                .ciudad(dto.ciudad())
                .pais(dto.pais())
                .password(dto.password())
                .rol(Rol.ANFITRION)
                .activo(true)
                .build();
        var created = usuarios.crear(u);
        try {
            String html = """
                <h2>Bienvenido como anfitrión</h2>
                <p>Hola %s,</p>
                <p>Tu registro como anfitrión fue exitoso. ¡Empieza a publicar tus alojamientos!</p>
                """.formatted(created.getNombre());
            mailService.sendAsync(EmailRequest.builder().to(created.getEmail()).subject("Bienvenido a GoHost (Anfitrión)").html(html).build());
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Autentica al usuario y retorna un JWT si las credenciales son válidas.
     *
     * @param dto email y password
     * @return TokenDTO con token, id de usuario y rol (HTTP 200) o 401 si falla
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto) {
        return usuarios.login(dto.email(), dto.password())
                .map(u -> {
                    // subject = id del usuario; incluimos claims útiles (rol y email)
                    String token = jwtUtils.generateToken(
                            u.getId().toString(),
                            Map.of("role", u.getRol().name(), "email", u.getEmail())
                    );

                    return ResponseEntity.ok(new TokenDTO(
                            token,
                            u.getId(),
                            u.getRol().name()
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
