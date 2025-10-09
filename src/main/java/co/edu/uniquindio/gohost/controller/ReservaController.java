package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.reservaDtos.ActualizarReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.ReservaResDTO;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * API de reservas.
 * El id del usuario (huésped/anfitrión) se toma del token (atributos en la request),
 * no de la URL, para evitar suplantaciones.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    /**
     * Crear una reserva para el huésped autenticado.
     * Devuelve DTO para evitar exponer entidades y prevenir LazyInitializationException.
     */
    @PostMapping
    @PreAuthorize("hasRole('HUESPED')")
    public ResponseEntity<ReservaResDTO> crear(@RequestBody CrearReservaDTO dto, HttpServletRequest request) {
        UUID huespedId = getUsuarioIdOrThrow(request);
        ReservaResDTO body = service.crearConDTO(dto.alojamientoId(), huespedId, dto.checkIn(), dto.checkOut());
        return ResponseEntity.ok(body);
    }

    /**
     * Listar reservas del huésped autenticado como DTO.
     */
    @GetMapping("/mias")
    @PreAuthorize("hasRole('HUESPED')")
    public ResponseEntity<Page<ReservaResDTO>> mias(HttpServletRequest request,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        UUID huespedId = getUsuarioIdOrThrow(request);
        Page<ReservaResDTO> body = service.listarPorHuespedConDTO(huespedId, PageRequest.of(page, size));
        return ResponseEntity.ok(body);
    }

    /**
     * Listar reservas de los alojamientos del anfitrión autenticado como DTO.
     */
    @GetMapping("/anfitrion")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Page<ReservaResDTO>> deMisAlojamientos(HttpServletRequest request,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        UUID anfitrionId = getUsuarioIdOrThrow(request);
        Page<ReservaResDTO> body = service.listarPorAnfitrionConDTO(anfitrionId, PageRequest.of(page, size));
        return ResponseEntity.ok(body);
    }

    /**
     * Obtener una reserva por ID como DTO.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservaResDTO> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(service.obtenerConDTO(id));
    }

    /**
     * Actualizar parcialmente una reserva y devolver DTO.
     * Para cambiar fechas, enviarlas ambas (checkIn y checkOut).
     */
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservaResDTO> actualizar(@PathVariable UUID id, @RequestBody ActualizarReservaDTO dto) {
        return ResponseEntity.ok(service.actualizarConDTO(id, dto.checkIn(), dto.checkOut(), dto.estado()));
    }

    /**
     * Cancelar una reserva por ID (idempotente).
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Utilidades ----------

    private UUID getUsuarioIdOrThrow(HttpServletRequest request) {
        Object attr = request.getAttribute("usuarioId");
        if (attr instanceof UUID uuid) {
            return uuid;
        }
        if (attr instanceof String s) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        }
        // Si el filtro no puso el atributo, señalamos 401 de forma clara.
        throw new IllegalStateException("No se encontró usuario autenticado (usuarioId).");
    }
}