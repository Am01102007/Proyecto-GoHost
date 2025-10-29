package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.*;
import co.edu.uniquindio.gohost.model.Alojamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface AlojamientoService {

    /**
     * Crea un nuevo alojamiento asociado a un anfitrión
     * @param anfitrionId ID del anfitrión propietario
     * @param alojamiento Datos del alojamiento a crear
     * @return Alojamiento creado
     */
    Alojamiento crear(UUID anfitrionId, Alojamiento alojamiento);

    /**
     * Lista todos los alojamientos con paginación.
     * Devuelve DTO para evitar LazyInitializationException.
     * @param pageable Configuración de paginación
     * @return Página de alojamientos como DTO de lectura
     */
    Page<AlojamientoResDTO> listar(Pageable pageable);

    /**
     * Lista alojamientos de un anfitrión específico.
     * Devuelve DTO para evitar LazyInitializationException.
     * @param anfitrionId ID del anfitrión
     * @param pageable Configuración de paginación
     * @return Página de alojamientos del anfitrión como DTO de lectura
     */
    Page<AlojamientoResDTO> listarPorAnfitrion(UUID anfitrionId, Pageable pageable);

    /**
     * Obtiene un alojamiento por su ID
     *
     * @param id ID del alojamiento
     * @return Alojamiento encontrado
     */
    AlojamientoResDTO obtener(UUID id);

    /**
     * Actualiza parcialmente un alojamiento
     *
     * @param id                 ID del alojamiento a actualizar
     * @param alojamientoParcial Datos parciales para actualizar
     * @return Alojamiento actualizado
     */
    AlojamientoResDTO actualizar(UUID id, Alojamiento alojamientoParcial);

    /**
     * Elimina un alojamiento
     * @param id ID del alojamiento a eliminar
     */
    void eliminar(UUID id);

    /**
     * Busca alojamientos aplicando filtros.
     * Devuelve DTO para evitar LazyInitializationException.
     * @param ciudad Ciudad donde buscar (puede ser null)
     * @param capacidad Capacidad mínima requerida (puede ser null)
     * @param pageable Configuración de paginación
     * @return Página de alojamientos como DTO de lectura
     */
    Page<AlojamientoResDTO> buscar(String ciudad, Integer capacidad, Pageable pageable);

    /**
     * Crea un alojamiento y devuelve DTO con ID generado
     *
     * @param anfitrionId ID del anfitrión propietario
     * @param dto         Datos del alojamiento a crear
     * @return DTO con el alojamiento creado
     */
    AlojamientoCreatedDTO crearConDTO(UUID anfitrionId, CrearAlojDTO dto);

    /**
     * Actualiza parcialmente un alojamiento
     *
     * @param id          ID del alojamiento a actualizar
     * @param dto         Datos parciales para actualizar
     * @param anfitrionId ID del anfitrión (para validar propiedad)
     * @return Alojamiento actualizado como DTO
     */
    AlojamientoResDTO actualizarConValidaciones(UUID id, EditAlojDTO dto, UUID anfitrionId);

    /**
     * Búsqueda avanzada de alojamientos con múltiples filtros.
     *
     * @param filtro DTO con todos los filtros de búsqueda
     * @return Página de alojamientos que cumplen los criterios
     */
    Page<AlojamientoResDTO> busquedaAvanzada(co.edu.uniquindio.gohost.dto.alojamientosDtos.FiltroAvanzadoDTO filtro);

    /**
     * Obtiene todas las ciudades disponibles para búsqueda predictiva.
     *
     * @return Lista de ciudades únicas
     */
    java.util.List<String> obtenerCiudades();

    /**
     * Búsqueda predictiva de ciudades.
     *
     * @param termino Término de búsqueda
     * @return Lista de ciudades que coinciden con el término
     */
    java.util.List<String> buscarCiudades(String termino);

    /**
     * Obtiene métricas de un alojamiento específico
     *
     * @param alojamientoId ID del alojamiento
     * @return Métricas del alojamiento
     */
    MetricasAlojamientoDTO obtenerMetricas(UUID alojamientoId);

    /**
     * Obtiene métricas de todos los alojamientos de un anfitrión con filtros de fecha
     *
     * @param anfitrionId ID del anfitrión
     * @param fechaInicio Fecha de inicio del filtro (opcional)
     * @param fechaFin Fecha de fin del filtro (opcional)
     * @return Lista de métricas por alojamiento
     */
    java.util.List<MetricasAlojamientoDTO> obtenerMetricasPorAnfitrion(UUID anfitrionId, 
                                                                       java.time.LocalDate fechaInicio, 
                                                                       java.time.LocalDate fechaFin);
}