package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.model.Alojamiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Lista todos los alojamientos con paginación
     * @param pageable Configuración de paginación
     * @return Página de alojamientos
     */
    Page<Alojamiento> listar(Pageable pageable);

    /**
     * Lista alojamientos de un anfitrión específico
     * @param anfitrionId ID del anfitrión
     * @param pageable Configuración de paginación
     * @return Página de alojamientos del anfitrión
     */
    Page<Alojamiento> listarPorAnfitrion(UUID anfitrionId, Pageable pageable);

    /**
     * Obtiene un alojamiento por su ID
     * @param id ID del alojamiento
     * @return Alojamiento encontrado
     */
    Alojamiento obtener(UUID id);

    /**
     * Actualiza parcialmente un alojamiento
     * @param id ID del alojamiento a actualizar
     * @param alojamientoParcial Datos parciales para actualizar
     * @return Alojamiento actualizado
     */
    Alojamiento actualizar(UUID id, Alojamiento alojamientoParcial);

    /**
     * Elimina un alojamiento
     * @param id ID del alojamiento a eliminar
     */
    void eliminar(UUID id);

    /**
     * Busca alojamientos aplicando filtros
     * @param ciudad Ciudad donde buscar (puede ser null)
     * @param capacidad Capacidad mínima requerida (puede ser null)
     * @param pageable Configuración de paginación
     * @return Página de alojamientos que cumplen los criterios
     */
    Page<Alojamiento> buscar(String ciudad, Integer capacidad, Pageable pageable);
}