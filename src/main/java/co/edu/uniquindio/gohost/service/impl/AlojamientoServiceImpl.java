package co.edu.uniquindio.gohost.service.impl;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.service.geocoding.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.UUID;

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
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> listar(Pageable pageable) {
        return alojamientoRepository.findAll(pageable);
    }

    /**
     * Lista alojamientos de un anfitrión (verifica existencia).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> listarPorAnfitrion(UUID anfitrionId, Pageable pageable) {
        if (!usuarioRepository.existsById(anfitrionId)) {
            throw new EntityNotFoundException("Anfitrión no encontrado: " + anfitrionId);
        }
        return alojamientoRepository.findByAnfitrionId(anfitrionId, pageable);
    }

    /**
     * Obtiene un alojamiento por id.
     */
    @Override
    @Transactional(readOnly = true)
    public Alojamiento obtener(UUID id) {
        return alojamientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no encontrado: " + id));
    }

    /**
     * Actualiza parcialmente un alojamiento.
     */
    @Override
    public Alojamiento actualizar(UUID id, Alojamiento parcial) {
        Alojamiento existente = obtener(id);

        if (StringUtils.hasText(parcial.getTitulo())) {
            existente.setTitulo(parcial.getTitulo());
        }
        if (StringUtils.hasText(parcial.getDescripcion())) {
            existente.setDescripcion(parcial.getDescripcion());
        }


        if (parcial.getDireccion() != null) {
            geocodificarDireccion(parcial.getDireccion());  // Geocodificar antes de guardar
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

        return alojamientoRepository.save(existente);
    }
    /**
     * Elimina un alojamiento por id.
     */
    @Override
    public void eliminar(UUID id) {
        if (!alojamientoRepository.existsById(id)) {
            throw new EntityNotFoundException("Alojamiento no encontrado: " + id);
        }
        alojamientoRepository.deleteById(id);
    }

    /**
     * Búsqueda flexible delegada al repositorio:
     * - ciudad: contiene (case-insensitive)
     * - capacidad: mínimo
     * Si ambos son nulos/vacíos, pagina todos.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> buscar(String ciudad, Integer capacidad, Pageable pageable) {
        boolean sinCiudad = !StringUtils.hasText(ciudad);
        boolean sinCapacidad = (capacidad == null);

        if (sinCiudad && sinCapacidad) {
            return alojamientoRepository.findAll(pageable);
        }
        return alojamientoRepository.search(sinCiudad ? null : ciudad, capacidad, pageable);
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
}
