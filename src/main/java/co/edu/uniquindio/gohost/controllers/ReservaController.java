
/*
 * ReservaController — Controlador REST para reservas
 * crear, listar, historial por usuario, obtener, actualizar y cancelar.
 */
package co.edu.uniquindio.gohost.controllers;

import co.edu.uniquindio.gohost.model.Reserva;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

//agrupamos todas las rutas bajo /reservas
@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    //servicio con la lógica de negocio
    private final ReservaService service;

    //DTOs minimalistas para no exponer la entidad completa
    public record CrearReservaReq(@NotNull UUID alojamientoId, @NotNull UUID huespedId,
                                  @NotNull LocalDate checkIn, @NotNull LocalDate checkOut) {}
    public record ActualizarReservaReq(LocalDate checkIn, LocalDate checkOut, String estado) {}

    //crea una nueva reserva (valida fechas y disponibilidad)
    @PostMapping public Reserva crear(@RequestBody CrearReservaReq req) {
        return service.crear(req.alojamientoId(), req.huespedId(), req.checkIn(), req.checkOut());
    }

    //lista paginada de reservas
    @GetMapping public Page<Reserva> listar(@RequestParam(defaultValue="0") int page,
                                            @RequestParam(defaultValue="10") int size) {
        return service.listar(PageRequest.of(page, size));
    }

    //historial de reservas de un usuario (paginado)
    @GetMapping("/usuario/{usuarioId}") public Page<Reserva> listarPorUsuario(@PathVariable UUID usuarioId,
                                                                              @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return service.listarPorUsuario(usuarioId, PageRequest.of(page, size));
    }

    //obtiene una reserva específica por id
    @GetMapping("/{reservaId}") public Reserva obtener(@PathVariable UUID reservaId) { return service.obtener(reservaId); }

    //actualiza fechas y/o estado de una reserva existente
    @PatchMapping("/{reservaId}") public Reserva actualizar(@PathVariable UUID reservaId, @RequestBody ActualizarReservaReq req) {
        return service.actualizar(reservaId, req.checkIn(), req.checkOut(), req.estado());
    }

    //cancela (elimina lógicamente) una reserva
    @DeleteMapping("/{reservaId}") public void cancelar(@PathVariable UUID reservaId) { service.cancelar(reservaId); }
}

