
package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.Comentario;
import org.springframework.data.domain.*;
import java.util.UUID;

/** Reglas de negocio de Comentario **/
public interface ComentarioService {
    Comentario crear(UUID alojamientoId, UUID autorId, String texto, int calificacion);
    Page<Comentario> listarPorAlojamiento(UUID alojamientoId, Pageable pageable);
    Comentario responder(UUID comentarioId, UUID anfitrionId, String respuesta);
}
