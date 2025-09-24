
/*
 * Alojamiento — Entidad JPA que representa un inmueble publicable
 * contiene info básica, precio/capacidad, anfitrión y colecciones (fotos, comentarios).
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

//tabla 'alojamientos' y sus relaciones
@Entity @Table(name="alojamientos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alojamiento {
    //id UUID
    @Id @GeneratedValue private java.util.UUID id;
    //metadatos principales
    @NotBlank @Size(min=3, max=120) private String titulo;
    @NotBlank @Size(min=10, max=2000) private String descripcion;
    @NotBlank private String ciudad;
    @NotBlank private String direccion;
    //atributos de negocio
    @Positive @Digits(integer=10, fraction=2) private BigDecimal precioNoche;
    @Positive @Max(50) private int capacidad;
    //relación hacia el usuario que publica
    @ManyToOne(optional=false, fetch=FetchType.LAZY) private Usuario anfitrion;
    //promedio (se podría calcular a partir de comentarios)
    private Double calificacionPromedio;
    //colección de fotos (cascade + orphanRemoval simplifican el CRUD)
    @OneToMany(mappedBy="alojamiento", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Foto> fotos = new ArrayList<>();
    //colección de comentarios
    @OneToMany(mappedBy="alojamiento", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Comentario> comentarios = new ArrayList<>();
}
