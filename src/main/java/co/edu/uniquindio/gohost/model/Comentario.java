
/*
 * Comentario — Review que deja un usuario sobre un alojamiento
 * guarda autor, alojamiento, texto y calificación (1-5).
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

//definición de la entidad comentario con validaciones
@Entity @Table(name="comentarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comentario {
    @Id @GeneratedValue private UUID id;
    //autor del comentario (usuario existente)
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Usuario autor;
    //alojamiento comentado
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Alojamiento alojamiento;
    //texto de la opinión (mín 3, máx 1000)
    @NotBlank @Size(min=3, max=1000) private String texto;
    //calificación entre 1 y 5
    @Min(1) @Max(5) private int calificacion;
    //timestamp de creación de solo lectura
    @Column(nullable=false, updatable=false) private Instant creadoEn;
    //setea creadoEn antes de insertar si viene nulo
    @PrePersist void pre(){ if (creadoEn==null) creadoEn = Instant.now(); }
}

