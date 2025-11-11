
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
@Table(name = "comentario", indexes = {
        @Index(name = "idx_comentario_alojamiento", columnList = "alojamiento_id"),
        @Index(name = "idx_comentario_autor", columnList = "autor_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comentario {

    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @NotBlank
    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Min(1) @Max(5)
    private int calificacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "respuesta")
    private String respuesta;

    @ManyToOne
    @JoinColumn(name = "respondido_por")
    private Usuario respondidoPor;

    @PrePersist
    void pre() { if (creadoEn == null) creadoEn = LocalDateTime.now(); }
}
