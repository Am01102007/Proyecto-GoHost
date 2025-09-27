
package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.CambioPasswordDTO;
import co.edu.uniquindio.gohost.dto.EditarUsuarioDTO;
import co.edu.uniquindio.gohost.dto.ResetPasswordDTO;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Operaciones de perfil de usuario **/
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    /** Lista paginada **/
    @GetMapping
    public Page<Usuario> listar(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    /** Obtener por ID **/
    @GetMapping("/{id}")
    public Usuario obtener(HttpServletRequest request) {
        UUID id = (UUID) request.getAttribute("usuarioId");
        return service.obtener(id);
    }
    /** Edición parcial **/
    @PatchMapping("/{id}")
    public Usuario editar(@PathVariable UUID id, @RequestBody EditarUsuarioDTO dto) {
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
        return service.actualizar(id, parcial);
    }

    /** Cambiar password **/
    @PutMapping("/{id}/password")
    public ResponseEntity<?> cambiarPassword(HttpServletRequest request, @RequestBody CambioPasswordDTO dto) {
        UUID id = (UUID) request.getAttribute("usuarioId");
        service.cambiarPassword(id, dto.actual(), dto.nueva());
        return ResponseEntity.ok().build();
    }

    /** Reset password **/
    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordDTO dto) {
        service.resetPassword(dto.email());
        return ResponseEntity.ok().build();
    }
}
