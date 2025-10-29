package co.edu.uniquindio.gohost.repository;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.MetricasAlojamientoDTO;
import co.edu.uniquindio.gohost.model.Alojamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    /** Búsqueda avanzada con múltiples filtros */
    @Query("""
        select distinct a from Alojamiento a
        left join fetch a.fotos
        where a.activo = true
          and (:#{#ciudad == null || #ciudad.isBlank()} = true or lower(a.direccion.ciudad) like lower(concat('%', :ciudad, '%')))
          and (:capacidad is null or a.capacidad >= :capacidad)
          and (:precioMinimo is null or a.precioNoche >= :precioMinimo)
          and (:precioMaximo is null or a.precioNoche <= :precioMaximo)
    """)
    Page<Alojamiento> busquedaAvanzada(@Param("ciudad") String ciudad,
                                       @Param("capacidad") Integer capacidad,
                                       @Param("precioMinimo") java.math.BigDecimal precioMinimo,
                                       @Param("precioMaximo") java.math.BigDecimal precioMaximo,
                                       Pageable pageable);

    /** Obtener ciudades únicas para búsqueda predictiva */
    @Query("select distinct a.direccion.ciudad from Alojamiento a where a.activo = true and a.direccion.ciudad is not null order by a.direccion.ciudad")
    List<String> findDistinctCiudades();

    /**
     * Obtiene ciudades únicas de alojamientos activos que contengan el texto dado (case-insensitive).
     * Útil para autocompletado/búsqueda predictiva.
     */
    @Query("""
        SELECT DISTINCT a.direccion.ciudad
        FROM Alojamiento a
        WHERE a.activo = true
          AND LOWER(a.direccion.ciudad) LIKE LOWER(CONCAT('%', :texto, '%'))
        ORDER BY a.direccion.ciudad
    """)
    List<String> buscarCiudades(@Param("texto") String texto);

    /**
     * Obtiene métricas de un alojamiento específico
     */
    @Query(value = """
        SELECT 
            a.titulo,
            COALESCE(AVG(c.calificacion), 0.0) as promedio_calificacion,
            COUNT(DISTINCT r.id) as total_reservas,
            COUNT(DISTINCT CASE WHEN r.estado = 'COMPLETADA' THEN r.id END) as reservas_completadas,
            COUNT(DISTINCT CASE WHEN r.estado = 'CANCELADA' THEN r.id END) as reservas_canceladas,
            COALESCE(SUM(CASE WHEN r.estado = 'COMPLETADA' THEN r.precio_total ELSE 0 END), 0) as ingresos_totales
        FROM alojamientos a
        LEFT JOIN reservas r ON r.alojamiento_id = a.id AND r.eliminada = false
        LEFT JOIN comentarios c ON c.alojamiento_id = a.id
        WHERE a.id = :alojamientoId AND a.activo = true
        GROUP BY a.id, a.titulo
    """, nativeQuery = true)
    Optional<Object[]> obtenerMetricasNative(@Param("alojamientoId") UUID alojamientoId);

    /**
     * Obtiene métricas de todos los alojamientos de un anfitrión con filtros de fecha
     */
    @Query(value = """
        SELECT 
            a.titulo,
            COALESCE(AVG(c.calificacion), 0.0) as promedio_calificacion,
            COUNT(DISTINCT r.id) as total_reservas,
            COUNT(DISTINCT CASE WHEN r.estado = 'COMPLETADA' THEN r.id END) as reservas_completadas,
            COUNT(DISTINCT CASE WHEN r.estado = 'CANCELADA' THEN r.id END) as reservas_canceladas,
            COALESCE(SUM(CASE WHEN r.estado = 'COMPLETADA' THEN r.precio_total ELSE 0 END), 0) as ingresos_totales
        FROM alojamientos a
        LEFT JOIN reservas r ON r.alojamiento_id = a.id AND r.eliminada = false 
            AND (:fechaInicio IS NULL OR r.check_in >= :fechaInicio)
            AND (:fechaFin IS NULL OR r.check_out <= :fechaFin)
        LEFT JOIN comentarios c ON c.alojamiento_id = a.id
        WHERE a.anfitrion_id = :anfitrionId AND a.activo = true
        GROUP BY a.id, a.titulo
    """, nativeQuery = true)
    List<Object[]> obtenerMetricasPorAnfitrionNative(@Param("anfitrionId") UUID anfitrionId,
                                                     @Param("fechaInicio") LocalDate fechaInicio,
                                                     @Param("fechaFin") LocalDate fechaFin);
}