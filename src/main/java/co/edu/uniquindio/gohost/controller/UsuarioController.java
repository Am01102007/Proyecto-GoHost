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
import co.edu.uniquindio.gohost.service.image.ImageService;
import co.edu.uniquindio.gohost.service.image.ImageUploadResult;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Operaciones de perfil de usuario.
 * Nota: el id del usuario autenticado se extrae del token (atributo "usuarioId" en la request).
 */
@RestController
@RequestMapping("/api/usuarios")
@Slf4j
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private AuthenticationHelper authHelper;

    @Autowired
    private ImageService imageService;

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
     * Edición parcial del perfil del usuario autenticado con carga opcional de foto.
     * PATCH /api/usuarios/me (multipart/form-data)
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioPerfilDTO> editar(
            HttpServletRequest request,
            @RequestPart("data") EditarUsuarioDTO dto,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoPerfil
    ) throws java.io.IOException {
        UUID id = authHelper.getAuthenticatedUserId(request);

        try {
            EditarUsuarioDTO dtoFinal = dto;
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                ImageUploadResult res = imageService.subirImagen(fotoPerfil);
                String url = res.secureUrl() != null ? res.secureUrl() : res.url();
                dtoFinal = new EditarUsuarioDTO(
                        dto.nombre(),
                        dto.apellidos(),
                        dto.telefono(),
                        dto.ciudad(),
                        dto.pais(),
                        dto.fechaNacimiento(),
                        dto.tipoDocumento(),
                        dto.numeroDocumento(),
                        url
                );
            }

            UsuarioPerfilDTO actualizado = service.actualizarPerfil(id, dtoFinal);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException iae) {
            log.warn("Datos inválidos al actualizar perfil: {}", iae.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (java.io.IOException ioe) {
            log.error("Fallo de proveedor de imágenes al actualizar perfil: {}", ioe.getMessage(), ioe);
            return ResponseEntity.status(502).build();
        }
    }

    /**
     * Compatibilidad retroactiva: edición con JSON puro.
     * PATCH /api/usuarios/me (application/json)
     */
    @PatchMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioPerfilDTO> editarJson(
            HttpServletRequest request,
            @RequestBody EditarUsuarioDTO dto
    ) {
        try {
            UUID id = authHelper.getAuthenticatedUserId(request);
            UsuarioPerfilDTO actualizado = service.actualizarPerfil(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException iae) {
            log.warn("Datos inválidos al actualizar perfil (JSON): {}", iae.getMessage());
            return ResponseEntity.badRequest().build();
        }
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
