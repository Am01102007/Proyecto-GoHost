
/*
 * Usuario — Entidad JPA que representa a una persona en el sistema
 * incluye datos personales, documento, rol y dirección embebida.
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

//definimos índices y restricciones únicas para email y documento
@Entity
@Table(name="usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_usuario_email", columnNames={"email"}),
                @UniqueConstraint(name="uk_usuario_doc", columnNames={"tipoDocumento","numeroDocumento"})
        },
        indexes = {
                @Index(name="idx_usuario_email", columnList="email"),
                @Index(name="idx_usuario_numdoc", columnList="numeroDocumento")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    //identificador único tipo UUID (lo genera la BD)
    @Id @GeneratedValue private UUID id;

    //nombres y apellidos obligatorios con longitudes mín/max
    @NotBlank @Size(min=2,max=80) private String nombres;
    @NotBlank @Size(min=2,max=80) private String apellidos;

    //género opcional (enum)
    @Enumerated(EnumType.STRING) private Genero genero;

    //fecha de nacimiento (debe ser en el pasado)
    @Past private LocalDate fechaNacimiento;

    //email obligatorio con formato válido, índice y unique
    @Email @NotBlank @Column(nullable=false) private String email;

    //teléfono opcional con patrón simple (solo números y símbolos comunes)
    @Size(max=30) @Pattern(regexp="^[0-9+()\-\s]{5,30}$") private String telefono;

    //tipo y número de documento (obligatorio)
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TipoDocumento tipoDocumento;
    @NotBlank @Size(min=3,max=30) private String numeroDocumento;

    //datos complementarios del documento
    @PastOrPresent private LocalDate fechaExpedicion;
    private String lugarExpedicion;
    private String nacionalidad;

    //rol del usuario (por defecto HUESPED en @PrePersist)
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Rol rol;

    //dirección embebida (sin tabla separada)
    @Valid @Embedded private Direccion direccion;

    //timestamp de creación solo lectura
    @Column(nullable=false, updatable=false) private Instant creadoEn;

    //hook que se ejecuta antes de insertar (setea creadoEn/rol si no vienen)
    @PrePersist void pre() {
        if (creadoEn==null) creadoEn = Instant.now();
        if (rol==null) rol = Rol.HUESPED;
    }
}
