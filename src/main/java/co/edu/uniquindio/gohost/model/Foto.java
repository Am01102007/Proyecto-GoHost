
/*
 * Foto — Entidad JPA que guarda una URL asociada a un Alojamiento
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

//tabla 'fotos' con relación muchos a uno hacia alojamiento
@Entity @Table(name="fotos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Foto {
    //id UUID generado automáticamente
    @Id @GeneratedValue private UUID id;
    //URL de la imagen (obligatoria, máx 500 chars)
    @NotBlank @Size(max=500) private String url;
    //referencia al alojamiento dueño de la foto
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Alojamiento alojamiento;
}

