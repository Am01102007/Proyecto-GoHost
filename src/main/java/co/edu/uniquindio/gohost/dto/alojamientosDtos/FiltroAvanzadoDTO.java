package co.edu.uniquindio.gohost.dto.alojamientosDtos;

import co.edu.uniquindio.gohost.model.ServicioAlojamiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para filtros avanzados de búsqueda de alojamientos.
 * Incluye filtros por ciudad, fechas, precio, servicios y paginación.
 */
public record FiltroAvanzadoDTO(
        // Filtro por ciudad (búsqueda predictiva)
        String ciudad,
        
        // Filtro por fechas de disponibilidad
        LocalDate fechaInicio,
        LocalDate fechaFin,
        
        // Filtro por rango de precios
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio mínimo debe ser mayor a 0")
        BigDecimal precioMinimo,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio máximo debe ser mayor a 0")
        BigDecimal precioMaximo,
        
        // Filtro por capacidad
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        Integer capacidad,
        
        // Filtros por servicios/amenidades
        List<ServicioAlojamiento> servicios,
        
        // Paginación
        Integer page,
        Integer size
) {
    
    /**
     * Valida que las fechas sean coherentes.
     */
    public boolean fechasValidas() {
        if (fechaInicio == null || fechaFin == null) {
            return true; // Si no se especifican fechas, no hay restricción
        }
        return !fechaInicio.isAfter(fechaFin);
    }
    
    /**
     * Valida que el rango de precios sea coherente.
     */
    public boolean preciosValidos() {
        if (precioMinimo == null || precioMaximo == null) {
            return true; // Si no se especifica rango, no hay restricción
        }
        return precioMinimo.compareTo(precioMaximo) <= 0;
    }
    
    /**
     * Indica si se debe filtrar por fechas.
     */
    public boolean tieneFiltroFechas() {
        return fechaInicio != null && fechaFin != null;
    }
    
    /**
     * Indica si se debe filtrar por precio.
     */
    public boolean tieneFiltroPrecios() {
        return precioMinimo != null || precioMaximo != null;
    }
    
    /**
     * Indica si se debe filtrar por servicios.
     */
    public boolean tieneFiltroServicios() {
        return servicios != null && !servicios.isEmpty();
    }
}