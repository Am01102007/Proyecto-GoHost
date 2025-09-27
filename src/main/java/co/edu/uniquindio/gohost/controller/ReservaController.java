
package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.ActualizarReservaDTO;
import co.edu.uniquindio.gohost.dto.CrearReservaDTO;
import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** API de reservas **/
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    /** Crear **/
    @PostMapping
    public Reserva crear(@RequestBody CrearReservaDTO dto, HttpServletRequest request) {
        UUID huespedId = (UUID) request.getAttribute("usuarioId");
        return service.crear(dto.alojamientoId(), huespedId, dto.checkIn(), dto.checkOut());
    }

    /** Por huésped **/
    @GetMapping("/huesped/{huespedId}")
    public Page<Reserva> mias(HttpServletRequest request,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        UUID huespedId = (UUID) request.getAttribute("usuarioId");
        return service.listarPorHuesped(huespedId, PageRequest.of(page, size));
    }

    /** Por anfitrión **/
    @GetMapping("/anfitrion/{anfitrionId}")
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

    /** Obtener **/
    @GetMapping("/{id}")
    public Reserva obtener(@PathVariable UUID id) { return service.obtener(id); }

    /** Actualizar **/
    @PatchMapping("/{id}")
    public Reserva actualizar(@PathVariable UUID id, @RequestBody ActualizarReservaDTO dto) { return service.actualizar(id, dto.checkIn(), dto.checkOut(), dto.estado()); }

    /** Cancelar **/
    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable UUID id) { service.cancelar(id); }
}
