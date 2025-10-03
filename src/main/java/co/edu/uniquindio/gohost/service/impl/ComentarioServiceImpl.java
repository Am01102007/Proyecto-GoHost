package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.model.Comentario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ComentarioRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.ComentarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Implementación JPA de {@link ComentarioService}.
 * Reglas:
 *  - Solo usuarios autenticados pueden crear comentarios.
 *  - Calificación válida: 1..5.
 *  - Solo el ANFITRIÓN dueño del alojamiento del comentario puede responder.
 */
@Service
@RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository repo;
    private final AlojamientoRepository alojRepo;
    private final UsuarioRepository usuarioRepo;

    /**
     * Crea un comentario para un alojamiento.
     *
     * @param alojamientoId id del alojamiento
     * @param autorId       id del usuario autor
     * @param texto         contenido del comentario
     * @param calificacion  calificación 1..5
     */
    @Override
    @Transactional
    public Comentario crear(UUID alojamientoId, UUID autorId, String texto, int calificacion) {
        if (!StringUtils.hasText(texto)) {
            throw new IllegalArgumentException("El texto del comentario no puede estar vacío");
        }
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }

        var alojamiento = alojRepo.findById(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("El alojamiento no existe"));

        var autor = usuarioRepo.findById(autorId)
                .orElseThrow(() -> new EntityNotFoundException("El autor no existe"));

        var comentario = Comentario.builder()
                .alojamiento(alojamiento)
                .autor(autor)
                .texto(texto.trim())
                .calificacion(calificacion)
                .build();

        return repo.save(comentario);
    }

    /**
     * Lista comentarios de un alojamiento en forma paginada.
     */
    @Override
    public Page<Comentario> listarPorAlojamiento(UUID alojamientoId, Pageable pageable) {
        return repo.findByAlojamientoId(alojamientoId, pageable);
    }

    /**
     * Responde un comentario. Solo el anfitrión del alojamiento al que pertenece
     * dicho comentario puede responder.
     *
     * @param comentarioId id del comentario a responder
     * @param anfitrionId  id del usuario anfitrión que responde
     * @param respuesta    contenido de la respuesta
     */
    @Override
    @Transactional
    public Comentario responder(UUID comentarioId, UUID anfitrionId, String respuesta) {
        if (!StringUtils.hasText(respuesta)) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía");
        }

        var comentario = repo.findById(comentarioId)
                .orElseThrow(() -> new EntityNotFoundException("El comentario no existe"));

        var anfitrion = usuarioRepo.findById(anfitrionId)
                .orElseThrow(() -> new EntityNotFoundException("El anfitrión no existe"));

        // Verificar que el anfitrión sea dueño del alojamiento del comentario
        var alojamiento = comentario.getAlojamiento();
        if (alojamiento == null || alojamiento.getAnfitrion() == null
                || !alojamiento.getAnfitrion().getId().equals(anfitrion.getId())) {
            throw new IllegalArgumentException("No está autorizado para responder este comentario");
        }

        comentario.setRespuesta(respuesta.trim());
        comentario.setRespondidoPor(anfitrion);

        return repo.save(comentario);
    }
}