package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import java.math.BigDecimal;

/**
 * DTO para métricas de alojamiento
 */
public record MetricasAlojamientoDTO(
        String titulo,
        Double promedioCalificacion,
        Long totalReservas,
        Long reservasCompletadas,
        Long reservasCanceladas,
        Double ingresosTotales
) {}