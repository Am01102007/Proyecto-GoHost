
package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Alojamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositorio de alojamientos con búsqueda por ciudad/capacidad **/
public interface AlojamientoRepository extends JpaRepository<Alojamiento, java.util.UUID> {

    /** Búsqueda flexible **/
    @Query("""
        select a from Alojamiento a
         where a.activo = true
           and (:#{#ciudad == null || #ciudad.isBlank()} = true or lower(a.direccion.ciudad) like lower(concat('%', :ciudad, '%')))
           and (:capacidad is null or a.capacidad >= :capacidad)
    """)
    Page<Alojamiento> search(@Param("ciudad") String ciudad,
                             @Param("capacidad") Integer capacidad,
                             Pageable pageable);

    /** Por anfitrión **/
    Page<Alojamiento> findByAnfitrionId(java.util.UUID anfitrionId, Pageable pageable);

    Page<Alojamiento> findByDireccionCiudadContainingIgnoreCaseAndCapacidadGreaterThanEqual(String ciudad, Integer capacidad, Pageable pageable);

    Page<Alojamiento> findByDireccionCiudadContainingIgnoreCase(String ciudad, Pageable pageable);

    Page<Alojamiento> findByCapacidadGreaterThanEqual(Integer capacidad, Pageable pageable);
}
