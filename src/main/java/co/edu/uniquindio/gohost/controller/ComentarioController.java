
package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.ComentarioDTO;
import co.edu.uniquindio.gohost.dto.RespuestaComentarioDTO;
import co.edu.uniquindio.gohost.model.Comentario;
import co.edu.uniquindio.gohost.service.ComentarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Comentarios y respuestas **/
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    /** Crear comentario **/
    @PostMapping("/alojamientos/{alojamientoId}/comentarios")
    public Comentario crear(@PathVariable UUID alojamientoId,
                            @RequestBody ComentarioDTO dto,
                            HttpServletRequest request) {
        UUID autorId = (UUID) request.getAttribute("usuarioId");
        return service.crear(alojamientoId, autorId, dto.texto(), dto.calificacion());
    }

    /** Listar comentarios **/
    @GetMapping("/alojamientos/{alojamientoId}/comentarios")
    public Page<Comentario> listar(@PathVariable UUID alojamientoId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return service.listarPorAlojamiento(alojamientoId, PageRequest.of(page, size));
    }

    /** Responder comentario **/
    @PostMapping("/comentarios/{comentarioId}/respuesta")
    public Comentario responder(@PathVariable UUID comentarioId,
                                @RequestBody RespuestaComentarioDTO dto,
                                HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"ANFITRION".equals(rol)) {
            throw new RuntimeException("Solo los anfitriones pueden responder comentarios");
        }
        UUID anfitrionId = (UUID) request.getAttribute("usuarioId");
        return service.responder(comentarioId, anfitrionId, dto.respuesta());
    }
}
