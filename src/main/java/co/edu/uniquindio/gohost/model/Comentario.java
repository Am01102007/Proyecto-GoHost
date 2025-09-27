
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Comentario de huésped con calificación y posible respuesta **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comentario {

    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Usuario autor;

    @ManyToOne(optional = false)
    private Alojamiento alojamiento;

    @NotBlank
    private String texto;

    @Min(1) @Max(5)
    private int calificacion;

    private LocalDateTime creadoEn;

    private String respuesta;

    @ManyToOne
    private Usuario respondidoPor;

    @PrePersist
    void pre() { if (creadoEn == null) creadoEn = LocalDateTime.now(); }
}
