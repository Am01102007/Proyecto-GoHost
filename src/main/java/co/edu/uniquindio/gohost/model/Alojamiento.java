package co.edu.uniquindio.gohost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alojamientos")
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

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Embedded
    private Direccion direccion;

    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNoche;

    @Column(nullable = false)
    private Integer capacidad;

    @ElementCollection
    @CollectionTable(
            name = "alojamiento_fotos",
            joinColumns = @JoinColumn(name = "alojamiento_id")
    )
    @Column(name = "foto_url", length = 500)
    private List<String> fotos;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
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
}