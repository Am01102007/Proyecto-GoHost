package co.edu.uniquindio.gohost.controller;

import co.edu.uniquindio.gohost.model.NotificacionRecordatorio;
import co.edu.uniquindio.gohost.service.RecordatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestionar recordatorios y preferencias de notificaciones.
 * Utiliza manejo centralizado de excepciones via RestExceptionHandler.
 */
@RestController
@RequestMapping("/api/recordatorios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    /**
     * Obtener todos los recordatorios de una reserva específica.
     */
    @GetMapping("/reserva/{reservaId}")
    public List<NotificacionRecordatorio> obtenerRecordatoriosDeReserva(@PathVariable UUID reservaId) {
        return recordatorioService.obtenerRecordatoriosDeReserva(reservaId);
    }

    /**
     * Cancelar todos los recordatorios de una reserva.
     */
    @DeleteMapping("/reserva/{reservaId}")
    public ResponseEntity<Void> cancelarRecordatoriosDeReserva(@PathVariable UUID reservaId) {
        recordatorioService.cancelarRecordatoriosDeReserva(reservaId);
        return ResponseEntity.ok().build();
    }

    /**
     * Marcar un recordatorio específico como enviado manualmente.
     */
    @PutMapping("/{recordatorioId}/marcar-enviado")
    public ResponseEntity<Void> marcarComoEnviado(@PathVariable UUID recordatorioId) {
        recordatorioService.marcarComoEnviado(recordatorioId);
        return ResponseEntity.ok().build();
    }

    /**
     * Marcar un recordatorio específico como fallido.
     */
    @PutMapping("/{recordatorioId}/marcar-fallido")
    public ResponseEntity<Void> marcarComoFallido(
            @PathVariable UUID recordatorioId,
            @RequestParam String mensajeError) {
        recordatorioService.marcarComoFallido(recordatorioId, mensajeError);
        return ResponseEntity.ok().build();
    }

    /**
     * Procesar manualmente recordatorios pendientes (para testing/admin).
     */
    @PostMapping("/procesar-pendientes")
    public ResponseEntity<String> procesarRecordatoriosPendientes() {
        recordatorioService.procesarRecordatoriosPendientes();
        return ResponseEntity.ok("Recordatorios procesados exitosamente");
    }

    /**
     * Reintentar recordatorios fallidos manualmente (para testing/admin).
     */
    @PostMapping("/reintentar-fallidos")
    public ResponseEntity<String> reintentarRecordatoriosFallidos() {
        recordatorioService.reintentarRecordatoriosFallidos();
        return ResponseEntity.ok("Recordatorios reintentados exitosamente");
    }
}