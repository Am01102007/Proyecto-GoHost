package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Servicio que define las reglas de negocio relacionadas con los {@link Comentario}.
 * Permite a los huéspedes dejar reseñas y a los anfitriones responderlas.
 */
public interface ComentarioService {

    /**
     * Crea un nuevo comentario asociado a un alojamiento y autor.
     *
     * @param alojamientoId id del alojamiento comentado
     * @param autorId       id del usuario autor (huésped)
     * @param texto         contenido textual del comentario
     * @param calificacion  puntuación numérica (ej. 1-5 estrellas)
     * @return comentario creado y persistido
     */
    Comentario crear(UUID alojamientoId, UUID autorId, String texto, int calificacion);

    /**
     * Lista los comentarios asociados a un alojamiento específico.
     *
     * @param alojamientoId id del alojamiento
     * @param pageable      configuración de paginación
     * @return página de comentarios
     */
    Page<Comentario> listarPorAlojamiento(UUID alojamientoId, Pageable pageable);

    /**
     * Permite al anfitrión responder a un comentario realizado por un huésped.
     *
     * @param comentarioId id del comentario a responder
     * @param anfitrionId  id del usuario anfitrión que responde
     * @param respuesta    texto de la respuesta
     * @return comentario actualizado con la respuesta
     */
    Comentario responder(UUID comentarioId, UUID anfitrionId, String respuesta);
}
