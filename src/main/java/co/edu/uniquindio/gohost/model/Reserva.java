
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/** Reserva con rango [checkIn, checkOut) **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {

    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Usuario huesped;

    @ManyToOne(optional = false)
    private Alojamiento alojamiento;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    private boolean eliminada = false;
}
