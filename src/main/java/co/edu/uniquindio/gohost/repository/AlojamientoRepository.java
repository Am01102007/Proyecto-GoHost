package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.model.Alojamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de alojamientos con búsqueda por ciudad/capacidad
 * y métodos con JOIN FETCH para cargar fotos (entidad Foto) y evitar LazyInitializationException.
 */
public interface AlojamientoRepository extends JpaRepository<Alojamiento, UUID> {

    /** Búsqueda flexible con fotos cargadas (JOIN FETCH) */
    @Query("""
        select distinct a from Alojamiento a
        left join fetch a.fotos
        where a.activo = true
          and (:#{#ciudad == null || #ciudad.isBlank()} = true or lower(a.direccion.ciudad) like lower(concat('%', :ciudad, '%')))
          and (:capacidad is null or a.capacidad >= :capacidad)
    """)
    Page<Alojamiento> searchWithFotos(@Param("ciudad") String ciudad,
                                      @Param("capacidad") Integer capacidad,
                                      Pageable pageable);

    /** Por anfitrión con fotos cargadas (JOIN FETCH) */
    @Query("select distinct a from Alojamiento a left join fetch a.fotos where a.anfitrion.id = :anfitrionId")
    Page<Alojamiento> findByAnfitrionIdWithFotos(@Param("anfitrionId") UUID anfitrionId, Pageable pageable);

    /** Obtener uno por ID con fotos cargadas (JOIN FETCH) */
    @Query("select distinct a from Alojamiento a left join fetch a.fotos where a.id = :id")
    Optional<Alojamiento> findByIdWithFotos(@Param("id") UUID id);

    /** Listar todos con fotos cargadas (JOIN FETCH) */
    @Query("select distinct a from Alojamiento a left join fetch a.fotos")
    Page<Alojamiento> findAllWithFotos(Pageable pageable);

    /* =========================================================
       Métodos anteriores (sin JOIN FETCH) – se conservan por si acaso
       ========================================================= */

    /** Búsqueda flexible sin JOIN FETCH (no usar en DTOs) */
    @Query("""
        select a from Alojamiento a
         where a.activo = true
           and (:#{#ciudad == null || #ciudad.isBlank()} = true or lower(a.direccion.ciudad) like lower(concat('%', :ciudad, '%')))
           and (:capacidad is null or a.capacidad >= :capacidad)
    """)
    Page<Alojamiento> search(@Param("ciudad") String ciudad,
                             @Param("capacidad") Integer capacidad,
                             Pageable pageable);

    /** Por anfitrión sin JOIN FETCH (no usar en DTOs) */
    Page<Alojamiento> findByAnfitrionId(UUID anfitrionId, Pageable pageable);
}