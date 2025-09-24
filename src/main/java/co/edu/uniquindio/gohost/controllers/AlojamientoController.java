
/*
 * AlojamientoController — Controlador REST para CRUD de alojamientos
 */
package co.edu.uniquindio.gohost.controllers;

import co.edu.uniquindio.gohost.model.Alojamiento;
import co.edu.uniquindio.gohost.service.AlojamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//@RestController expone endpoints; @RequestMapping define el prefijo de ruta
@RestController
@RequestMapping("/alojamientos")
@RequiredArgsConstructor
public class AlojamientoController {

    //inyectamos el servicio que contiene la lógica de negocio
    private final AlojamientoService service;

    //DTO de creación para no exponer toda la entidad al cliente
    public record CrearAlojamientoReq(
            String titulo, String descripcion, String ciudad, String direccion,
            java.math.BigDecimal precioNoche, int capacidad, UUID anfitrionId, List<String> fotos) {}

    //Crea un alojamiento. Convierte el DTO a entidad y delega al servicio
    @PostMapping
    public Alojamiento crear(@Valid @RequestBody CrearAlojamientoReq req) {
        Alojamiento a = Alojamiento.builder()
                .titulo(req.titulo()).descripcion(req.descripcion())
                .ciudad(req.ciudad()).direccion(req.direccion())
                .precioNoche(req.precioNoche()).capacidad(req.capacidad())
                .build();
        return service.crear(a, req.anfitrionId(), req.fotos());
    }

    //Lista alojamientos con filtro opcional por ciudad y paginación
    @GetMapping
    public Page<Alojamiento> listar(@RequestParam(required=false) String ciudad,
                                    @RequestParam(defaultValue="0") int page,
                                    @RequestParam(defaultValue="10") int size) {
        return service.listar(ciudad, PageRequest.of(page, size));
    }

    //Obtiene un alojamiento por id (404 si no existe)
    @GetMapping("/{alojamientoId}")
    public Alojamiento obtener(@PathVariable UUID alojamientoId) { return service.obtener(alojamientoId); }

    //Reemplaza por completo (PUT) los datos del alojamiento
    @PutMapping("/{alojamientoId}")
    public Alojamiento reemplazar(@PathVariable UUID alojamientoId, @Valid @RequestBody Alojamiento body) {
        return service.reemplazar(alojamientoId, body);
    }

    //Actualiza parcialmente (PATCH) solo los campos presentes
    @PatchMapping("/{alojamientoId}")
    public Alojamiento actualizar(@PathVariable UUID alojamientoId, @RequestBody Alojamiento parcial) {
        return service.actualizarParcial(alojamientoId, parcial);
    }

    //Elimina definitivamente el alojamiento
    @DeleteMapping("/{alojamientoId}")
    public void eliminar(@PathVariable UUID alojamientoId) { service.eliminar(alojamientoId); }
}
