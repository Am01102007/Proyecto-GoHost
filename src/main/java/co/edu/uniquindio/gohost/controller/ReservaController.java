package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.dto.reservaDtos.ActualizarReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.CrearReservaDTO;
import co.edu.uniquindio.gohost.dto.reservaDtos.ReservaResDTO;
import co.edu.uniquindio.gohost.model.EstadoReserva;
import co.edu.uniquindio.gohost.security.AuthenticationHelper;
import co.edu.uniquindio.gohost.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * API de reservas.
 * El id del usuario (huésped/anfitrión) se toma del token (atributos en la request),
 * no de la URL, para evitar suplantaciones.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService service;

    @Autowired
    private AuthenticationHelper authHelper;

    /**
     * Crear una reserva para el huésped autenticado.
     * Devuelve DTO para evitar exponer entidades y prevenir LazyInitializationException.
     */
    @PostMapping
    @PreAuthorize("hasRole('HUESPED')")
    public ResponseEntity<ReservaResDTO> crear(HttpServletRequest request, @RequestBody CrearReservaDTO dto) {
        UUID huespedId = authHelper.getAuthenticatedUserId(request);
        ReservaResDTO reserva = service.crearConDTO(huespedId, dto);
        return ResponseEntity.ok(reserva);
    }

    /**
     * Listar reservas del huésped autenticado como DTO con filtros y ordenamiento.
     */
    @GetMapping("/mias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReservaResDTO>> deMisReservas(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) EstadoReserva estado) {
        
        UUID huespedId = authHelper.getAuthenticatedUserId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservaResDTO> reservas = service.listarPorHuespedConDTO(huespedId, fechaInicio, fechaFin, estado, pageable);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Listar reservas de los alojamientos del anfitrión autenticado como DTO.
     */
    @GetMapping("/anfitrion")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Page<ReservaResDTO>> deMisAlojamientos(HttpServletRequest request,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        Page<ReservaResDTO> body = service.listarPorAnfitrionConDTO(anfitrionId, PageRequest.of(page, size));
        return ResponseEntity.ok(body);
    }

    /**
     * Listar reservas de un alojamiento específico del anfitrión autenticado.
     */
    @GetMapping("/alojamiento/{alojamientoId}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Page<ReservaResDTO>> listarPorAlojamiento(
            HttpServletRequest request,
            @PathVariable UUID alojamientoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UUID anfitrionId = authHelper.getAuthenticatedUserId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservaResDTO> reservas = service.listarPorAlojamientoConDTO(alojamientoId, anfitrionId, pageable);
        return ResponseEntity.ok(reservas);
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
     * Actualizar una reserva (anfitrión).
     */
    @PutMapping("/{reservaId}")
    @PreAuthorize("hasRole('ANFITRION')")
    public ResponseEntity<Void> actualizar(
            HttpServletRequest request,
            @PathVariable UUID reservaId,
            @RequestBody ActualizarReservaDTO dto) {
        
        // Convertir DTO a parámetros individuales para el método existente
        ReservaResDTO resultado = service.actualizarConDTO(reservaId, dto.checkIn(), dto.checkOut(), dto.estado());
        return ResponseEntity.noContent().build();
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
    // (Método getUsuarioIdOrThrow removido - ahora se usa AuthenticationHelper)
}
