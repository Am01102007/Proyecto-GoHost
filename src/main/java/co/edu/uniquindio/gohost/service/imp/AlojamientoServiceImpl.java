package co.edu.uniquindio.gohost.service.imp;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Usuario;
import co.edu.uniquindio.gohost.repository.AlojamientoRepository;
import co.edu.uniquindio.gohost.repository.UsuarioRepository;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AlojamientoServiceImpl implements AlojamientoService {

    private final AlojamientoRepository alojamientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Alojamiento crear(UUID anfitrionId, Alojamiento alojamiento) {
        // Verificar que el anfitrión existe
        Usuario anfitrion = usuarioRepository.findById(anfitrionId)
                .orElseThrow(() -> new RuntimeException("Anfitrión no encontrado con ID: " + anfitrionId));

        // Asignar el anfitrión al alojamiento
        alojamiento.setAnfitrion(anfitrion);

        // Establecer valores por defecto
        if (alojamiento.getActivo() == null) {
            alojamiento.setActivo(true);
        }

        return alojamientoRepository.save(alojamiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> listar(Pageable pageable) {
        return alojamientoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> listarPorAnfitrion(UUID anfitrionId, Pageable pageable) {
        // Verificar que el anfitrión existe
        if (!usuarioRepository.existsById(anfitrionId)) {
            throw new RuntimeException("Anfitrión no encontrado con ID: " + anfitrionId);
        }

        return alojamientoRepository.findByAnfitrionId(anfitrionId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Alojamiento obtener(UUID id) {
        return alojamientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado con ID: " + id));
    }

    @Override
    public Alojamiento actualizar(UUID id, Alojamiento alojamientoParcial) {
        Alojamiento existente = obtener(id);

        // Actualizar solo los campos no nulos
        if (StringUtils.hasText(alojamientoParcial.getTitulo())) {
            existente.setTitulo(alojamientoParcial.getTitulo());
        }

        if (StringUtils.hasText(alojamientoParcial.getDescripcion())) {
            existente.setDescripcion(alojamientoParcial.getDescripcion());
        }

        if (alojamientoParcial.getDireccion() != null) {
            existente.setDireccion(alojamientoParcial.getDireccion());
        }

        if (alojamientoParcial.getPrecioNoche() != null) {
            existente.setPrecioNoche(alojamientoParcial.getPrecioNoche());
        }

        if (alojamientoParcial.getCapacidad() != null) {
            existente.setCapacidad(alojamientoParcial.getCapacidad());
        }

        if (alojamientoParcial.getFotos() != null) {
            existente.setFotos(alojamientoParcial.getFotos());
        }

        if (alojamientoParcial.getActivo() != null) {
            existente.setActivo(alojamientoParcial.getActivo());
        }

        return alojamientoRepository.save(existente);
    }

    @Override
    public void eliminar(UUID id) {
        if (!alojamientoRepository.existsById(id)) {
            throw new RuntimeException("Alojamiento no encontrado con ID: " + id);
        }

        alojamientoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alojamiento> buscar(String ciudad, Integer capacidad, Pageable pageable) {
        // Si no hay filtros, devolver todos
        if (!StringUtils.hasText(ciudad) && capacidad == null) {
            return alojamientoRepository.findAll(pageable);
        }

        // Aplicar filtros según los parámetros disponibles
        if (StringUtils.hasText(ciudad) && capacidad != null) {
            return alojamientoRepository.findByDireccionCiudadContainingIgnoreCaseAndCapacidadGreaterThanEqual(
                    ciudad, capacidad, pageable);
        } else if (StringUtils.hasText(ciudad)) {
            return alojamientoRepository.findByDireccionCiudadContainingIgnoreCase(ciudad, pageable);
        } else {
            return alojamientoRepository.findByCapacidadGreaterThanEqual(capacidad, pageable);
        }
    }
}