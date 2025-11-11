package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reserva con rango [checkIn, checkOut)
 */
@Entity
@Table(name = "reserva", indexes = {
        @Index(name = "idx_reserva_alojamiento", columnList = "alojamiento_id"),
        @Index(name = "idx_reserva_check_in", columnList = "check_in"),
        @Index(name = "idx_reserva_check_out", columnList = "check_out"),
        @Index(name = "idx_reserva_estado", columnList = "estado")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "huesped_id", nullable = false)
    private Usuario huesped;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @NotNull
    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @NotNull
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @NotNull
    @Min(value = 1, message = "El número de huéspedes debe ser al menos 1")
    @Column(name = "numero_huespedes", nullable = false)
    private Integer numeroHuespedes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "eliminada", nullable = false)
    @Builder.Default
    private boolean eliminada = false;

}
