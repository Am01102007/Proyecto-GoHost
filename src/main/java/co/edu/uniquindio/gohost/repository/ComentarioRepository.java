package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repositorio para la entidad {@link Comentario}.
 * Permite operaciones CRUD básicas y consultas específicas.
 */
public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    /**
     * Lista los comentarios de un alojamiento específico.
     *
     * @param alojamientoId identificador del alojamiento.
     * @param pageable      información de paginación.
     * @return página de comentarios asociados al alojamiento.
     */
    Page<Comentario> findByAlojamientoId(UUID alojamientoId, Pageable pageable);

    /**
     * Calcula el promedio de calificaciones de los alojamientos
     * de un anfitrión específico.
     *
     * @param anfitrionId identificador del anfitrión.
     * @return promedio de calificaciones, o null si no existen comentarios.
     */
    @Query("SELECT AVG(c.calificacion) FROM Comentario c WHERE c.alojamiento.anfitrion.id = :anfitrionId")
    Double promedioCalifPorAnfitrion(@Param("anfitrionId") UUID anfitrionId);
}
