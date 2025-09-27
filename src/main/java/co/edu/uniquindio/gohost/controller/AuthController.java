
package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.LoginDTO;
import co.edu.uniquindio.gohost.dto.RegistroDTO;
import co.edu.uniquindio.gohost.dto.TokenDTO;
import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.security.JwtUtil;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador para registro y autenticación **/
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarios;

    private final JwtUtil jwtUtil;


    /**
     * Registra un usuario con rol HUESPED
     **/
    @PostMapping("/register")
    public Usuario register(@Valid @RequestBody RegistroDTO dto) {
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
                .build();
        return usuarios.crear(u);
    }
    /** Loggin del usuario usando JWT**/
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO dto) {
        return usuarios.login(dto.email(), dto.password())
                .map(u -> ResponseEntity.ok(new TokenDTO(
                        jwtUtil.generarToken(u.getId(), u.getRol().name()),
                        u.getId(),
                        u.getRol().name()
                )))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
