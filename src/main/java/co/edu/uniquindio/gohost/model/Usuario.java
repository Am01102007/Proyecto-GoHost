package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * Usuario — Entidad de usuario con identidad, contacto, credenciales, rol
 * y ubicación geográfica (dirección + coordenadas).
 * ============================================================================
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    /** ID único **/
    @Id
    @GeneratedValue
    private UUID id;

    /** Tipo de documento **/
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;

    /** Número de documento (único) **/
    @Column(unique = true, length = 40)
    private String numeroDocumento;

    /** Correo (único) **/
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    /** Nombres **/
    @NotBlank
    @Size(min = 2, max = 80)
    private String nombre;

    /** Apellidos **/
    private String apellidos;

    /** Fecha de nacimiento **/
    private LocalDate fechaNacimiento;

    /** Teléfono **/
    private String telefono;

    /** Ciudad **/
    private String ciudad;

    /** País **/
    private String pais;

    /** Foto de perfil (URL) **/
    @Column(length = 500)
    private String fotoPerfil;

    /**
     * Dirección completa (usada para geocodificación).
     * Puede ser calle + número + barrio.
     */
    @Column(length = 255)
    private String direccion;

    /**
     * Coordenadas WGS84 obtenidas al geocodificar la dirección.
     */
    private Double latitud;
    private Double longitud;

    /** Contraseña **/
    @NotBlank
    private String password;

    /** Rol **/
    @Enumerated(EnumType.STRING)
    private Rol rol;

    /** Activo **/
    @Builder.Default
    private boolean activo = true;

    /** Creado **/
    private LocalDateTime creadoEn;

    /** Actualizado **/
    private LocalDateTime actualizadoEn;

    /** Inicializa timestamps/rol **/
    @PrePersist
    void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (rol == null) rol = Rol.HUESPED;
    }

    /** Actualiza timestamp **/
    @PreUpdate
    void preUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
