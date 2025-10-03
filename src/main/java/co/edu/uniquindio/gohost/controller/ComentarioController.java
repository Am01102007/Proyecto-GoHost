package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.comentariosDtos.ComentarioDTO;
import co.edu.uniquindio.gohost.dto.comentariosDtos.RespuestaComentarioDTO;
import co.edu.uniquindio.gohost.model.Comentario;
import co.edu.uniquindio.gohost.service.ComentarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestión de comentarios en alojamientos.
 * - Los huéspedes pueden crear comentarios.
 * - Los anfitriones pueden responder comentarios.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService service;

    /**
     * Crear un nuevo comentario en un alojamiento.
     * El autor del comentario se obtiene del token (atributo usuarioId en la request).
     *
     * @param alojamientoId ID del alojamiento
     * @param dto           DTO con el texto y calificación
     * @param request       request con los atributos de autenticación
     * @return comentario creado
     */
    @PostMapping("/alojamientos/{alojamientoId}/comentarios")
    public Comentario crear(@PathVariable UUID alojamientoId,
                            @RequestBody ComentarioDTO dto,
                            HttpServletRequest request) {
        UUID autorId = (UUID) request.getAttribute("usuarioId"); // del token
        return service.crear(alojamientoId, autorId, dto.texto(), dto.calificacion());
    }

    /**
     * Listar todos los comentarios de un alojamiento (paginados).
     *
     * @param alojamientoId ID del alojamiento
     * @param page          número de página (default = 0)
     * @param size          tamaño de página (default = 10)
     * @return página de comentarios
     */
    @GetMapping("/alojamientos/{alojamientoId}/comentarios")
    public Page<Comentario> listar(@PathVariable UUID alojamientoId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return service.listarPorAlojamiento(alojamientoId, PageRequest.of(page, size));
    }

    /**
     * Responder a un comentario existente.
     * Solo los ANFITRIONES autenticados pueden responder.
     *
     * @param comentarioId ID del comentario a responder
     * @param dto          DTO con el texto de la respuesta
     * @param request      request con los atributos de autenticación
     * @return comentario actualizado con la respuesta
     */
    @PostMapping("/comentarios/{comentarioId}/respuesta")
    public Comentario responder(@PathVariable UUID comentarioId,
                                @RequestBody RespuestaComentarioDTO dto,
                                HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"ANFITRION".equals(rol)) {
            throw new RuntimeException("Solo los anfitriones pueden responder comentarios");
        }

        UUID anfitrionId = (UUID) request.getAttribute("usuarioId"); // del token
        return service.responder(comentarioId, anfitrionId, dto.respuesta());
    }
}
