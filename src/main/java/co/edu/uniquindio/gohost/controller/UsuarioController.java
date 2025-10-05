package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.usuarioDtos.CambioPasswordDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.EditarUsuarioDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.ResetPasswordDTO;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Operaciones de perfil de usuario.
 * Nota: el id del usuario autenticado se extrae del token (atributo "usuarioId" en la request).
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    /** Lista paginada de usuarios (si corresponde a tu caso de uso/rol). */
    @GetMapping
    public Page<Usuario> listar(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return service.listar(PageRequest.of(page, size));
    }


    /**
     * Obtener el perfil del usuario autenticado.
     * GET /api/usuarios/me
     */
    @GetMapping("/me")
    public Usuario obtener(HttpServletRequest request) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID id2= UUID.fromString(id);
        return service.obtener(id2);
    }

    /**
     * Edición parcial del perfil del usuario autenticado.
     * PATCH /api/usuarios/me
     */
    @PatchMapping("/me")
    public Usuario editar(HttpServletRequest request, @RequestBody EditarUsuarioDTO dto) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID id2 = UUID.fromString(id);

        var parcial = new Usuario();
        parcial.setNombre(dto.nombre());
        parcial.setApellidos(dto.apellidos());
        parcial.setTelefono(dto.telefono());
        parcial.setCiudad(dto.ciudad());
        parcial.setPais(dto.pais());
        parcial.setFechaNacimiento(dto.fechaNacimiento());
        parcial.setTipoDocumento(dto.tipoDocumento());
        parcial.setNumeroDocumento(dto.numeroDocumento());
        parcial.setEmail(dto.email());

        return service.actualizar(id2, parcial);
    }
    /**
     * Cambio de contraseña del usuario autenticado.
     * PUT /api/usuarios/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> cambiarPassword(HttpServletRequest request, @RequestBody CambioPasswordDTO dto) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID id2 = UUID.fromString(id);
        service.cambiarPassword(id2, dto.actual(), dto.nueva());
        return ResponseEntity.ok().build();
    }

    /**
     * Solicitar reseteo de contraseña por email (flujo público).
     * POST /api/usuarios/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordDTO dto) {
        service.resetPassword(dto.email());
        return ResponseEntity.ok().build();
    }

}
