package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.alojamientosDtos.CrearAlojDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.EditAlojDTO;
import co.edu.uniquindio.gohost.dto.alojamientosDtos.FiltroBusquedaDTO;
import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Controlador REST para gestión de alojamientos.
 * Nota: el id del anfitrión se obtiene del token (atributos en la request),
 * no desde la URL, para evitar suplantaciones.
 */
@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoController {

    private final AlojamientoService service;

    /**
     * Crea un alojamiento para el anfitrión autenticado.
     * Requiere rol ANFITRION (validado automáticamente por Spring Security).
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @PostMapping
    public Alojamiento crear(@RequestBody CrearAlojDTO dto, HttpServletRequest request) {

        UUID anfitrionId = (UUID) request.getAttribute("usuarioId"); // viene del token (JWTFilter)

        var alojamiento = Alojamiento.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .direccion(dto.toDireccion())
                .precioNoche(dto.precioNoche())
                .capacidad(dto.capacidad())
                .fotos(dto.fotos() == null ? new ArrayList<>() : dto.fotos())
                .build();

        return service.crear(anfitrionId, alojamiento);
    }

    /** Lista paginada de todos los alojamientos. */
    @GetMapping
    public Page<Alojamiento> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    /**
     * Lista los alojamientos del anfitrión autenticado.
     * El anfitrión se toma del token (no de la URL).
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @GetMapping("/anfitrion")
    public Page<Alojamiento> porAnfitrion(HttpServletRequest request,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        UUID anfitrionId = (UUID) request.getAttribute("usuarioId"); // del token
        return service.listarPorAnfitrion(anfitrionId, PageRequest.of(page, size));
    }

    /** Obtiene un alojamiento por su ID. */
    @GetMapping("/{id}")
    public Alojamiento obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    /**
     * Actualiza parcialmente un alojamiento por ID.
     * (Valida permisos en la capa de servicio si corresponde).
     */
    @PreAuthorize("hasRole('ANFITRION')")
    @PatchMapping("/{id}")
    public Alojamiento actualizar(@PathVariable UUID id, @RequestBody EditAlojDTO dto) {
        var parcial = new Alojamiento();
        parcial.setTitulo(dto.titulo());
        parcial.setDescripcion(dto.descripcion());
        parcial.setDireccion(dto.toDireccion());
        parcial.setPrecioNoche(dto.precioNoche());
        parcial.setCapacidad(dto.capacidad());
        if (dto.fotos() != null) parcial.setFotos(dto.fotos());
        if (dto.activo() != null) parcial.setActivo(dto.activo());
        return service.actualizar(id, parcial);
    }

    /** Elimina un alojamiento por ID. */
    @PreAuthorize("hasRole('ANFITRION')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        service.eliminar(id);
    }

    /**
     * Búsqueda con filtros (ciudad, capacidad) y paginación.
     */
    @PostMapping("/search")
    public Page<Alojamiento> buscar(@RequestBody FiltroBusquedaDTO f) {
        int page = f.page() == null ? 0 : f.page();
        int size = f.size() == null ? 10 : f.size();
        return service.buscar(f.ciudad(), f.capacidad(), PageRequest.of(page, size));
    }
}