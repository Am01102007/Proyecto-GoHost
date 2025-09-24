
/*
 * UsuarioController — Controlador REST para CRUD de usuarios
 * expone endpoints para crear, listar, obtener, actualizar, reemplazar y eliminar.
 */
package co.edu.uniquindio.gohost.controllers;

import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//prefijo /usuarios para todas las rutas de este recurso
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    //servicio inyectado que maneja la lógica
    private final UsuarioService service;

    //crea un usuario a partir del cuerpo JSON validado
    @PostMapping public Usuario crear(@Valid @RequestBody Usuario req) { return service.crear(req); }

    //lista paginada (page/size) de usuarios
    @GetMapping public Page<Usuario> listar(@RequestParam(defaultValue="0") int page,
                                            @RequestParam(defaultValue="10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    //obtiene un usuario por su UUID
    @GetMapping("/{usuarioId}") public Usuario obtener(@PathVariable UUID usuarioId) { return service.obtener(usuarioId); }

    //reemplaza completamente (PUT) los datos del usuario
    @PutMapping("/{usuarioId}") public Usuario reemplazar(@PathVariable UUID usuarioId, @Valid @RequestBody Usuario body) {
        return service.reemplazar(usuarioId, body);
    }

    //actualiza parcialmente (PATCH) campos no nulos
    @PatchMapping("/{usuarioId}") public Usuario actualizar(@PathVariable UUID usuarioId, @RequestBody Usuario parcial) {
        return service.actualizar(usuarioId, parcial);
    }

    //elimina el usuario por id
    @DeleteMapping("/{usuarioId}") public void eliminar(@PathVariable UUID usuarioId) { service.eliminar(usuarioId); }
}

