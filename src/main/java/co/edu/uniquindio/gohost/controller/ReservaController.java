package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.reservaDtos.ActualizarReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * API de reservas.
 * Nota: El id del usuario (huésped/anfitrión) se toma del token (atributos en la request),
 * no de la URL, para evitar suplantaciones.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    /**
     * Crear una reserva para el huésped autenticado.
     */
    @PostMapping
    public Reserva crear(@RequestBody CrearReservaDTO dto, HttpServletRequest request) {
        UUID huespedId = (UUID) request.getAttribute("usuarioId");
        return service.crear(dto.alojamientoId(), huespedId, dto.checkIn(), dto.checkOut());
    }

    /**
     * Listar reservas del huésped autenticado.
     */
    @GetMapping("/mias")
    public Page<Reserva> mias(HttpServletRequest request,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        UUID huespedId = (UUID) request.getAttribute("usuarioId");
        return service.listarPorHuesped(huespedId, PageRequest.of(page, size));
    }

    /**
     * Listar reservas de los alojamientos del anfitrión autenticado.
     * Requiere rol ANFITRION.
     */
    @GetMapping("/anfitrion")
    public Page<Reserva> deMisAlojamientos(HttpServletRequest request,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        String rol = (String) request.getAttribute("rol");
        if (!"ANFITRION".equals(rol)) {
            throw new RuntimeException("Solo los anfitriones pueden ver reservas de sus alojamientos");
        }
        UUID anfitrionId = (UUID) request.getAttribute("usuarioId");
        return service.listarPorAnfitrion(anfitrionId, PageRequest.of(page, size));
    }

    /**
     * Obtener una reserva por ID.
     */
    @GetMapping("/{id}")
    public Reserva obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    /**
     * Actualizar parcialmente una reserva.
     */
    @PatchMapping("/{id}")
    public Reserva actualizar(@PathVariable UUID id, @RequestBody ActualizarReservaDTO dto) {
        return service.actualizar(id, dto.checkIn(), dto.checkOut(), dto.estado());
    }

    /**
     * Cancelar una reserva por ID.
     */
    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable UUID id) {
        service.cancelar(id);
    }
}
