
/*
 * Reserva — Entidad JPA que une huésped + alojamiento en un rango de fechas
 * evita traslapes a nivel de servicio y calcula total en base a noches * precio.
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

//índice por alojamiento para consultar disponibilidad rápido
@Entity @Table(name="reservas", indexes=@Index(name="idx_reserva_alojamiento", columnList="alojamiento_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {
    @Id @GeneratedValue private java.util.UUID id;
    //relaciones obligatorias
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Usuario huesped;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Alojamiento alojamiento;
    //rango de fechas
    @NotNull private LocalDate checkIn;
    @NotNull private LocalDate checkOut;
    //estado del ciclo de vida
    @Enumerated(EnumType.STRING) @Column(nullable=false) private co.edu.uniquindio.gohost.model.EstadoReserva estado;
    //total en dinero (usar BigDecimal para evitar errores de precisión)
    @Positive @Digits(integer=10, fraction=2) private BigDecimal total;
    //timestamp de creación de solo lectura
    @Column(nullable=false, updatable=false) private Instant creadoEn;
    //establece defaults al insertar
    @PrePersist void pre(){ if (creadoEn==null) creadoEn=Instant.now(); if (estado==null) estado=co.edu.uniquindio.gohost.model.EstadoReserva.PENDIENTE; }
}
