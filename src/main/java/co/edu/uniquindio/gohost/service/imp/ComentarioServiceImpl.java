
package co.edu.uniquindio.gohost.service.imp;

import co.edu.uniquindio.gohost.model.*;
import co.edu.uniquindio.gohost.repository.*;
import co.edu.uniquindio.gohost.service.ComentarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.UUID;

/** Implementación JPA de ComentarioService **/
@Service @RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository repo;
    private final AlojamientoRepository alojRepo;
    private final UsuarioRepository usuarioRepo;

    public Comentario crear(UUID alojamientoId, UUID autorId, String texto, int calificacion) {
        var a = alojRepo.findById(alojamientoId).orElseThrow(() -> new EntityNotFoundException("Alojamiento no existe"));
        var u = usuarioRepo.findById(autorId).orElseThrow(() -> new EntityNotFoundException("Autor no existe"));
        return repo.save(Comentario.builder().alojamiento(a).autor(u).texto(texto).calificacion(calificacion).build());
    }

    public Page<Comentario> listarPorAlojamiento(UUID alojamientoId, Pageable pageable) { return repo.findByAlojamientoId(alojamientoId, pageable); }

    public Comentario responder(UUID comentarioId, UUID anfitrionId, String respuesta) {
        var c = repo.findById(comentarioId).orElseThrow(() -> new EntityNotFoundException("Comentario no existe"));
        c.setRespuesta(respuesta);
        c.setRespondidoPor(usuarioRepo.findById(anfitrionId).orElseThrow(() -> new EntityNotFoundException("Anfitrión no existe")));
        return repo.save(c);
    }
}
