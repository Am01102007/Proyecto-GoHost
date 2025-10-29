package co.edu.uniquindio.gohost.service;

import co.edu.uniquindio.gohost.service.impl.RecordatorioServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio de programación para ejecutar tareas automáticas de recordatorios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final RecordatorioServiceImpl recordatorioService;

    /**
     * Ejecuta cada 30 minutos para procesar recordatorios pendientes.
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos en milisegundos
    public void procesarRecordatoriosPendientes() {
        log.info("Iniciando procesamiento de recordatorios pendientes...");
        try {
            recordatorioService.procesarRecordatoriosPendientes();
            log.info("Procesamiento de recordatorios completado exitosamente");
        } catch (Exception e) {
            log.error("Error durante el procesamiento de recordatorios: {}", e.getMessage(), e);
        }
    }

    /**
     * Ejecuta cada 2 horas para reintentar recordatorios fallidos.
     */
    @Scheduled(fixedRate = 7200000) // 2 horas en milisegundos
    public void reintentarRecordatoriosFallidos() {
        log.info("Iniciando reintento de recordatorios fallidos...");
        try {
            recordatorioService.reintentarRecordatoriosFallidos();
            log.info("Reintento de recordatorios fallidos completado");
        } catch (Exception e) {
            log.error("Error durante el reintento de recordatorios fallidos: {}", e.getMessage(), e);
        }
    }

    /**
     * Ejecuta diariamente a las 6:00 AM para limpiar recordatorios antiguos.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void limpiarRecordatoriosAntiguos() {
        log.info("Iniciando limpieza de recordatorios antiguos...");
        // Esta funcionalidad se puede implementar más adelante si es necesaria
        log.info("Limpieza de recordatorios completada");
    }
}