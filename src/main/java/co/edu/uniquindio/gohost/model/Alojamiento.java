package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "alojamientos",
        indexes = {
                @Index(name = "idx_aloj_anfitrion", columnList = "anfitrion_id"),
                @Index(name = "idx_aloj_ciudad", columnList = "direccion_ciudad") // depende de @Embeddable Direccion
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"anfitrion"})
@EqualsAndHashCode(of = "id")
public class Alojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    /**
     * Se asume que Direccion es @Embeddable con campos:
     *  - direccion (línea)
     *  - ciudad
     *  - pais
     *  - latitud, longitud (opcional)
     * Sugerencia: en Direccion anotar @Column(name="direccion_ciudad") para el campo ciudad,
     * así coincide con el índice idx_aloj_ciudad.
     */
    @Embedded
    private Direccion direccion;

    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNoche;

    @Column(nullable = false)
    private Integer capacidad;

    /**
     * URLs (por ejemplo de Cloudinary). Se guarda el orden de inserción.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "alojamiento_fotos",
            joinColumns = @JoinColumn(name = "alojamiento_id", nullable = false)
    )
    @OrderColumn(name = "orden")
    @Column(name = "foto_url", length = 500, nullable = false)
    @Builder.Default
    private List<String> fotos = new ArrayList<>();

    /**
     * Servicios/amenidades disponibles en el alojamiento.
     */
    @ElementCollection(targetClass = ServicioAlojamiento.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "alojamiento_servicios",
            joinColumns = @JoinColumn(name = "alojamiento_id", nullable = false)
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "servicio", nullable = false)
    @Builder.Default
    private List<ServicioAlojamiento> servicios = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos de conveniencia
    public String getCiudad() {
        return direccion != null ? direccion.getCiudad() : null;
    }

    public String getPais() {
        return direccion != null ? direccion.getPais() : null;
    }

    public boolean esActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void agregarFoto(String url) {
        if (url != null && !url.isBlank()) {
            this.fotos.add(url);
        }
    }

    public void eliminarFoto(String url) {
        if (url != null) {
            this.fotos.remove(url);
        }
    }

    public void agregarServicio(ServicioAlojamiento servicio) {
        if (servicio != null && !this.servicios.contains(servicio)) {
            this.servicios.add(servicio);
        }
    }

    public void eliminarServicio(ServicioAlojamiento servicio) {
        if (servicio != null) {
            this.servicios.remove(servicio);
        }
    }

    public boolean tieneServicio(ServicioAlojamiento servicio) {
        return servicio != null && this.servicios.contains(servicio);
    }
}