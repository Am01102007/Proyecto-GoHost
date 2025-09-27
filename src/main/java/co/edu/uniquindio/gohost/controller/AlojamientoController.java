
package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.CrearAlojDTO;
import co.edu.uniquindio.gohost.dto.EditAlojDTO;
import co.edu.uniquindio.gohost.dto.FiltroBusquedaDTO;
import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.model.Direccion;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

/** Gestión de alojamientos y búsqueda **/
@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoController {

    private final AlojamientoService service;

    /** Crea alojamiento **/
    @PostMapping("/{anfitrionId}")
    public Alojamiento crear(@RequestBody CrearAlojDTO dto, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        if (!"ANFITRION".equals(rol)) {
            throw new RuntimeException("Solo los anfitriones pueden crear alojamientos");
        }
        UUID anfitrionId = (UUID) request.getAttribute("usuarioId");
        var a = Alojamiento.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .direccion(new Direccion(dto.ciudad(), dto.pais(), dto.calle(), dto.zip()))
                .precioNoche(dto.precioNoche())
                .capacidad(dto.capacidad())
                .fotos(dto.fotos() == null ? new ArrayList<>() : dto.fotos())
                .build();
        return service.crear(anfitrionId, a);
    }

    /** Listar **/
    @GetMapping
    public Page<Alojamiento> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    /** Por anfitrión **/
    @GetMapping("/anfitrion/{anfitrionId}")
    public Page<Alojamiento> porAnfitrion(HttpServletRequest request,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        UUID anfitrionId = (UUID) request.getAttribute("usuarioId");
        return service.listarPorAnfitrion(anfitrionId, PageRequest.of(page, size));
    }

    /** Obtener **/
    @GetMapping("/{id}")
    public Alojamiento obtener(@PathVariable UUID id) { return service.obtener(id); }

    /** Actualizar **/
    @PatchMapping("/{id}")
    public Alojamiento actualizar(@PathVariable UUID id, @RequestBody EditAlojDTO dto) {
        var parcial = new Alojamiento();
        parcial.setTitulo(dto.titulo());
        parcial.setDescripcion(dto.descripcion());
        parcial.setDireccion(new Direccion(dto.ciudad(), dto.pais(), dto.calle(), dto.zip()));
        parcial.setPrecioNoche(dto.precioNoche());
        parcial.setCapacidad(dto.capacidad());
        if (dto.fotos() != null) parcial.setFotos(dto.fotos());
        if (dto.activo() != null) parcial.setActivo(dto.activo());
        return service.actualizar(id, parcial);
    }

    /** Eliminar **/
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) { service.eliminar(id); }

    /** Buscar con filtros **/
    @PostMapping("/search")
    public Page<Alojamiento> buscar(@RequestBody FiltroBusquedaDTO f) {
        int page = f.page() == null ? 0 : f.page();
        int size = f.size() == null ? 10 : f.size();
        return service.buscar(f.ciudad(), f.capacidad(), PageRequest.of(page, size));
    }
}
