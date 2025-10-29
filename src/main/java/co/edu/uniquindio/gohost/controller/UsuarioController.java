package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.usuarioDtos.CambioPasswordDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.ConfirmarResetPasswordDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioPerfilDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.UsuarioResDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.EditarUsuarioDTO;
import co.edu.uniquindio.gohost.dto.usuarioDtos.ResetPasswordDTO;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.security.AuthenticationHelper;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Operaciones de perfil de usuario.
 * Nota: el id del usuario autenticado se extrae del token (atributo "usuarioId" en la request).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private AuthenticationHelper authHelper;

    /** 
     * Lista paginada de usuarios como DTO.
     * Excluye información sensible y evita problemas de lazy loading.
     */
    @GetMapping
    public Page<UsuarioResDTO> listar(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return service.listarConDTO(PageRequest.of(page, size));
    }


    /**
     * Obtener el perfil del usuario autenticado.
     * GET /api/usuarios/me
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilDTO> obtenerPerfil(Authentication authentication) {
        UUID usuarioId = UUID.fromString(authentication.getName());
        UsuarioPerfilDTO perfil = service.obtenerPerfil(usuarioId);
        return ResponseEntity.ok(perfil);
    }

    /**
     * Edición parcial del perfil del usuario autenticado.
     * PATCH /api/usuarios/me
     */
    @PatchMapping("/me")
    public UsuarioPerfilDTO editar(HttpServletRequest request, @RequestBody EditarUsuarioDTO dto) {
        UUID id = authHelper.getAuthenticatedUserId(request);
        return service.actualizarPerfil(id, dto);
    }
    /**
     * Cambio de contraseña del usuario autenticado.
     * PUT /api/usuarios/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> cambiarPassword(HttpServletRequest request, @RequestBody CambioPasswordDTO dto) {
        UUID id = authHelper.getAuthenticatedUserId(request);
        service.cambiarPassword(id, dto.actual(), dto.nueva());
        return ResponseEntity.ok().build();
    }

    /**
     * Solicita el reseteo de contraseña enviando un token al correo del usuario.
     * Flujo público: no requiere autenticación.
     */
    @PostMapping("/password/reset")
    public ResponseEntity<String> solicitarResetPassword(@RequestBody ResetPasswordDTO dto) {
        service.resetPassword(dto.email());
        return ResponseEntity.accepted().body("Se ha enviado un enlace de recuperación a tu correo.");
    }

    @PutMapping("/password/confirm")
    public ResponseEntity<String> confirmarResetPassword(@RequestBody ConfirmarResetPasswordDTO dto) {
        service.confirmarResetPassword(dto.token(), dto.nuevaPassword());
        return ResponseEntity.ok("Contraseña restablecida correctamente");
    }



}
