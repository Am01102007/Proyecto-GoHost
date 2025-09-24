
/*
 * ComentarioController — Controlador REST para comentarios
 * crea y lista comentarios por alojamiento; CRUD por id.
 */
package co.edu.uniquindio.gohost.controllers;

import co.edu.uniquindio.gohost.model.Comentario;
import co.edu.uniquindio.gohost.service.ComentarioService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//rutas sin prefijo global (mostramos ambos estilos: con y sin @RequestMapping de clase)
@RestController @RequiredArgsConstructor
public class ComentarioController {

    //servicio de comentarios
    private final ComentarioService service;

    //DTOs para crear y actualizar
    public record CrearComentarioReq(@NotNull UUID autorId, @NotBlank String texto, @Min(1) @Max(5) int calificacion) {}
    public record ActualizarComentarioReq(String texto, Integer calificacion) {}

    //lista comentarios por alojamiento con paginación
    @GetMapping("/alojamientos/{alojamientoId}/comentarios")
    public Page<Comentario> listar(@PathVariable UUID alojamientoId, @RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="10") int size) {
        return service.listarPorAlojamiento(alojamientoId, PageRequest.of(page, size));
    }

    //crea un comentario en un alojamiento
    @PostMapping("/alojamientos/{alojamientoId}/comentarios")
    public Comentario crear(@PathVariable UUID alojamientoId, @RequestBody CrearComentarioReq req) {
        return service.crear(alojamientoId, req.autorId(), req.texto(), req.calificacion());
    }

    //obtiene, actualiza y elimina un comentario por id
    @GetMapping("/comentarios/{comentarioId}") public Comentario obtener(@PathVariable UUID comentarioId) { return service.obtener(comentarioId); }
    @PatchMapping("/comentarios/{comentarioId}") public Comentario actualizar(@PathVariable UUID comentarioId, @RequestBody ActualizarComentarioReq req) {
        return service.actualizar(comentarioId, req.texto(), req.calificacion());
    }
    @DeleteMapping("/comentarios/{comentarioId}") public void eliminar(@PathVariable UUID comentarioId) { service.eliminar(comentarioId); }
}
