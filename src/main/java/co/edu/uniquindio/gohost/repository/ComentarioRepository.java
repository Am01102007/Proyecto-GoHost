
package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio de comentarios **/
public interface ComentarioRepository extends JpaRepository<Comentario, java.util.UUID> {

    /** Lista por alojamiento **/
    Page<Comentario> findByAlojamientoId(java.util.UUID alojamientoId, Pageable pageable);

    /** Promedio por anfitrión **/
    @Query("select avg(c.calificacion) from Comentario c where c.alojamiento.anfitrion.id = :anfitrionId")
    Double promedioCalifPorAnfitrion(@org.springframework.data.repository.query.Param("anfitrionId") java.util.UUID anfitrionId);
}
