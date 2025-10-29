package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.*;
import jakarta.persistence.EntityNotFoundException;
import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.ServicioAlojamiento;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.ReservaRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import co.edu.uniquindio.gohost.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de {@link AlojamientoService}.
 * Reglas:
 *  - Solo un usuario con rol ANFITRION puede crear alojamientos.
 *  - Campos se actualizan parcialmente (solo los no nulos / no vacíos).
 *  - Búsqueda flexible por ciudad/capacidad delegada al repositorio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlojamientoServiceImpl implements AlojamientoService {

    private final AlojamientoRepository alojamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final GeocodingService geocodingService;
    /**
     * Crea un alojamiento para el anfitrión indicado.
     */
    @Override
    public Alojamiento crear(UUID anfitrionId, Alojamiento alojamiento) {
        // 1) Verificar anfitrión
        Usuario anfitrion = usuarioRepository.findById(anfitrionId)
                .orElseThrow(() -> new EntityNotFoundException("Anfitrión no encontrado: " + anfitrionId));

        if (anfitrion.getRol() != Rol.ANFITRION) {
            throw new IllegalArgumentException("El usuario no tiene rol ANFITRION");
        }

        // 2) Defaults
        if (alojamiento.getActivo() == null) {
            alojamiento.setActivo(true);
        }
        if (alojamiento.getFotos() == null) {
            alojamiento.setFotos(new ArrayList<>());
        }

        // 3) GEOCODIFICAR DIRECCIÓN (NUEVO)
        if (alojamiento.getDireccion() != null) {
            geocodificarDireccion(alojamiento.getDireccion());
        }

        // 4) Asociar y persistir
        alojamiento.setAnfitrion(anfitrion);
        return alojamientoRepository.save(alojamiento);
    }

    /**
     * Lista paginada de alojamientos.
     * Devuelve DTO para evitar LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlojamientoResDTO> listar(Pageable pageable) {
        return alojamientoRepository.findAllWithFotos(pageable).map(this::toRes);
    }


    /**
     * Lista alojamientos de un anfitrión (verifica existencia).
     * Devuelve DTO para evitar LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlojamientoResDTO> listarPorAnfitrion(UUID anfitrionId, Pageable pageable) {
        if (!usuarioRepository.existsById(anfitrionId)) {
            throw new EntityNotFoundException("Anfitrión no encontrado: " + anfitrionId);
        }
        return alojamientoRepository.findByAnfitrionIdWithFotos(anfitrionId, pageable).map(this::toRes);
    }

    /**
     * Obtiene un alojamiento por id.
     * Devuelve DTO para evitar LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public AlojamientoResDTO obtener(UUID id) {
        Alojamiento a = alojamientoRepository.findByIdWithFotos(id)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no encontrado: " + id));
        return toRes(a);
    }

    /**
     * Actualiza parcialmente un alojamiento.
     * Devuelve DTO para evitar LazyInitializationException.
     */
    @Override
    public AlojamientoResDTO actualizar(UUID id, Alojamiento parcial) {
        Alojamiento existente = obtenerEntidad(id); // método auxiliar privado

        if (StringUtils.hasText(parcial.getTitulo())) {
            existente.setTitulo(parcial.getTitulo());
        }
        if (StringUtils.hasText(parcial.getDescripcion())) {
            existente.setDescripcion(parcial.getDescripcion());
        }

        if (parcial.getDireccion() != null) {
            geocodificarDireccion(parcial.getDireccion());
            existente.setDireccion(parcial.getDireccion());
        }

        if (parcial.getPrecioNoche() != null) {
            existente.setPrecioNoche(parcial.getPrecioNoche());
        }
        if (parcial.getCapacidad() != null) {
            existente.setCapacidad(parcial.getCapacidad());
        }
        if (parcial.getFotos() != null) {
            existente.setFotos(parcial.getFotos());
        }
        if (parcial.getActivo() != null) {
            existente.setActivo(parcial.getActivo());
        }

        Alojamiento guardado = alojamientoRepository.save(existente);
        // recargamos con fotos antes de mapear
        return toRes(alojamientoRepository.findByIdWithFotos(guardado.getId()).orElseThrow());
    }

    /**
     * Elimina un alojamiento por id (soft delete).
     * Valida que no tenga reservas futuras antes de eliminar.
     */
    @Override
    @Transactional
    public void eliminar(UUID id) {
        Alojamiento alojamiento = alojamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no encontrado: " + id));
        
        // Verificar si tiene reservas futuras activas
        boolean tieneReservasFuturas = reservaRepository.existsReservasFuturas(id, LocalDate.now());
        if (tieneReservasFuturas) {
            throw new IllegalStateException("No se puede eliminar el alojamiento porque tiene reservas futuras");
        }
        
        // Soft delete: marcar como inactivo
        alojamiento.setActivo(false);
        alojamientoRepository.save(alojamiento);
    }


    /**
     * Búsqueda flexible con filtros.
     * Devuelve DTO para evitar LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlojamientoResDTO> buscar(String ciudad, Integer capacidad, Pageable pageable) {
        boolean sinCiudad = !StringUtils.hasText(ciudad);
        boolean sinCapacidad = (capacidad == null);

        Page<Alojamiento> page;
        if (sinCiudad && sinCapacidad) {
            page = alojamientoRepository.findAllWithFotos(pageable);
        } else {
            page = alojamientoRepository.searchWithFotos(sinCiudad ? null : ciudad, capacidad, pageable);
        }
        return page.map(this::toRes);
    }

    /**
     * Geocodifica la dirección y actualiza las coordenadas automáticamente.
     * No falla la operación si la geocodificación no funciona.
     */
    private void geocodificarDireccion(Direccion direccion) {
        if (direccion == null) {
            return;
        }

        try {
            String direccionCompleta = direccion.getDireccionCompleta();

            if (!StringUtils.hasText(direccionCompleta)) {
                log.debug("Dirección vacía, no se puede geocodificar");
                return;
            }

            var coordenadas = geocodingService.obtenerCoordenadas(
                    direccionCompleta,
                    direccion.getCiudad(),
                    direccion.getPais()
            );

            if (coordenadas != null) {
                direccion.setLatitud(coordenadas.latitud());
                direccion.setLongitud(coordenadas.longitud());
                log.info("Geocodificación exitosa: {} -> [{}, {}]",
                        direccionCompleta, coordenadas.latitud(), coordenadas.longitud());
            } else {
                log.warn("No se obtuvieron coordenadas para: {}", direccionCompleta);
            }
        } catch (Exception e) {
            log.error("Error geocodificando dirección: {}",
                    direccion.getDireccionCompleta(), e);
            // No lanzamos excepción - la geocodificación es opcional
        }
    }
    private AlojamientoResDTO toRes(Alojamiento a) {
        return new AlojamientoResDTO(
                a.getId(),
                a.getTitulo(),
                a.getDescripcion(),
                a.getPrecioNoche(),
                a.getCapacidad(),
                a.getFotos() == null ? List.of() : a.getFotos(),
                a.getServicios() == null ? List.of() : a.getServicios(),
                a.getDireccion() == null ? "Sin ciudad" : a.getDireccion().getCiudad(),
                a.getAnfitrion() == null ? null : a.getAnfitrion().getId()
        );
    }
    /**
     * Método auxiliar para obtener la entidad sin exponerla.
     * Usado internamente por actualizar.
     */
    private Alojamiento obtenerEntidad(UUID id) {
        return alojamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no encontrado: " + id));
    }

    @Override
    public AlojamientoCreatedDTO crearConDTO(UUID anfitrionId, CrearAlojDTO dto) {
        // Mapear DTO a entidad
        var alojamiento = Alojamiento.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .direccion(dto.toDireccion())
                .precioNoche(dto.precioNoche())
                .capacidad(dto.capacidad())
                .fotos(dto.fotos() == null ? new ArrayList<>() : dto.fotos())
                .servicios(dto.servicios() == null ? new ArrayList<>() : dto.servicios())
                .build();
        
        Alojamiento creado = crear(anfitrionId, alojamiento);
        
        return new AlojamientoCreatedDTO(
                creado.getId(),
                creado.getTitulo(),
                creado.getDescripcion(),
                creado.getDireccion(),
                creado.getPrecioNoche(),
                creado.getCapacidad(),
                creado.getFotos(),
                creado.getServicios(),
                creado.getActivo(),
                creado.getAnfitrion().getId(),
                creado.getFechaCreacion()
        );
    }

    /**
     * Actualiza un alojamiento con validaciones de propiedad y estado activo.
     */
    @Override
    public AlojamientoResDTO actualizarConValidaciones(UUID id, EditAlojDTO dto, UUID anfitrionId) {
        Alojamiento existente = obtenerEntidad(id);

        // Validar que el alojamiento esté activo
        if (!existente.getActivo()) {
            throw new IllegalStateException("No se puede editar un alojamiento inactivo");
        }

        // Validar que el usuario sea el propietario del alojamiento
        if (!existente.getAnfitrion().getId().equals(anfitrionId)) {
            throw new IllegalArgumentException("Solo el propietario puede editar este alojamiento");
        }

        // Aplicar las actualizaciones parciales desde el DTO
        if (StringUtils.hasText(dto.titulo())) {
            existente.setTitulo(dto.titulo());
        }
        if (StringUtils.hasText(dto.descripcion())) {
            existente.setDescripcion(dto.descripcion());
        }

        if (dto.toDireccion() != null) {
            geocodificarDireccion(dto.toDireccion());
            existente.setDireccion(dto.toDireccion());
        }

        if (dto.precioNoche() != null) {
            existente.setPrecioNoche(dto.precioNoche());
        }
        if (dto.capacidad() != null) {
            existente.setCapacidad(dto.capacidad());
        }
        
        // Manejo especial para fotos: reemplazar completamente si se proporcionan
        if (dto.fotos() != null) {
            existente.setFotos(new ArrayList<>(dto.fotos()));
        }
        
        if (dto.activo() != null) {
            existente.setActivo(dto.activo());
        }

        Alojamiento guardado = alojamientoRepository.save(existente);
        // recargamos con fotos antes de mapear
        return toRes(alojamientoRepository.findByIdWithFotos(guardado.getId()).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlojamientoResDTO> busquedaAvanzada(FiltroAvanzadoDTO filtro) {
        // Validaciones del filtro
        if (!filtro.fechasValidas()) {
            throw new IllegalArgumentException("Las fechas de inicio y fin no son válidas");
        }
        if (!filtro.preciosValidos()) {
            throw new IllegalArgumentException("El rango de precios no es válido");
        }

        int page = filtro.page() == null ? 0 : filtro.page();
        int size = filtro.size() == null ? 10 : filtro.size();
        Pageable pageable = PageRequest.of(page, size);

        Page<Alojamiento> resultados;

        // Si hay filtro por servicios, necesitamos una consulta más compleja
        if (filtro.tieneFiltroServicios()) {
            resultados = busquedaConServicios(filtro, pageable);
        } else {
            // Usar la consulta básica sin servicios
            resultados = alojamientoRepository.busquedaAvanzada(
                    filtro.ciudad(),
                    filtro.capacidad(),
                    filtro.precioMinimo(),
                    filtro.precioMaximo(),
                    pageable
            );
        }

        // Si hay filtro por fechas, aplicar filtro adicional
        if (filtro.tieneFiltroFechas()) {
            resultados = filtrarPorDisponibilidad(resultados, filtro.fechaInicio(), filtro.fechaFin());
        }

        return resultados.map(this::toRes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerCiudades() {
        return alojamientoRepository.findDistinctCiudades();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> buscarCiudades(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerCiudades();
        }
        return alojamientoRepository.buscarCiudades(termino.trim());
    }

    /**
     * Búsqueda con filtro de servicios (requiere consulta más compleja).
     */
    private Page<Alojamiento> busquedaConServicios(FiltroAvanzadoDTO filtro, Pageable pageable) {
        // Por ahora, implementamos una versión simplificada
        // En una implementación completa, se podría usar Criteria API o consultas nativas
        Page<Alojamiento> resultados = alojamientoRepository.busquedaAvanzada(
                filtro.ciudad(),
                filtro.capacidad(),
                filtro.precioMinimo(),
                filtro.precioMaximo(),
                pageable
        );

        // Filtrar por servicios en memoria (no es la solución más eficiente para grandes volúmenes)
        List<Alojamiento> filtrados = resultados.getContent().stream()
                .filter(alojamiento -> filtro.servicios().stream()
                        .allMatch(servicio -> alojamiento.getServicios().contains(servicio)))
                .toList();

        return new PageImpl<>(filtrados, pageable, filtrados.size());
    }

    /**
     * Filtra alojamientos por disponibilidad en las fechas especificadas.
     * Nota: Esta es una implementación simplificada. En un sistema real,
     * se consultaría una tabla de reservas para verificar disponibilidad.
     */
    private Page<Alojamiento> filtrarPorDisponibilidad(Page<Alojamiento> alojamientos, 
                                                       LocalDate fechaInicio, 
                                                       LocalDate fechaFin) {
        // Por ahora, retornamos todos los alojamientos
        // En una implementación completa, se verificaría contra las reservas existentes
        return alojamientos;
    }

    @Override
    @Transactional(readOnly = true)
    public MetricasAlojamientoDTO obtenerMetricas(UUID alojamientoId) {
        Optional<Object[]> resultado = alojamientoRepository.obtenerMetricasNative(alojamientoId);
        
        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("Alojamiento no encontrado: " + alojamientoId);
        }
        
        Object[] row = resultado.get();
        return new MetricasAlojamientoDTO(
            (String) row[0],           // titulo
            ((Number) row[1]).doubleValue(),  // promedio_calificacion
            ((Number) row[2]).longValue(),    // total_reservas
            ((Number) row[3]).longValue(),    // reservas_completadas
            ((Number) row[4]).longValue(),    // reservas_canceladas
            ((Number) row[5]).doubleValue()   // ingresos_totales
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricasAlojamientoDTO> obtenerMetricasPorAnfitrion(UUID anfitrionId, 
                                                                   LocalDate fechaInicio, 
                                                                   LocalDate fechaFin) {
        // Validar que el anfitrión existe
        if (!usuarioRepository.existsById(anfitrionId)) {
            throw new IllegalArgumentException("Anfitrión no encontrado: " + anfitrionId);
        }
        
        List<Object[]> resultados = alojamientoRepository.obtenerMetricasPorAnfitrionNative(anfitrionId, fechaInicio, fechaFin);
        
        return resultados.stream()
            .map(row -> new MetricasAlojamientoDTO(
                (String) row[0],           // titulo
                ((Number) row[1]).doubleValue(),  // promedio_calificacion
                ((Number) row[2]).longValue(),    // total_reservas
                ((Number) row[3]).longValue(),    // reservas_completadas
                ((Number) row[4]).longValue(),    // reservas_canceladas
                ((Number) row[5]).doubleValue()   // ingresos_totales
            ))
            .collect(Collectors.toList());
    }
}
