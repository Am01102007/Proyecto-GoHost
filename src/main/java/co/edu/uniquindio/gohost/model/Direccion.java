package co.edu.uniquindio.gohost.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * ============================================================================
 * Dirección postal embebida en entidades como Alojamiento o Usuario.
 * ============================================================================
 *
 * Almacena información básica (calle, ciudad, país) y coordenadas geográficas.
 * Es compatible con la geocodificación automática (latitud/longitud).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    /** Ciudad donde se ubica el alojamiento **/
    @Column(name = "direccion_ciudad", length = 120)
    private String ciudad;

    /** País de residencia (ISO-2 o nombre completo) **/
    @Column(name = "direccion_pais", length = 120)
    private String pais;

    /** Calle y número **/
    @Column(name = "direccion_calle", length = 200)
    private String calle;

    /** Código postal (opcional) **/
    @Column(name = "direccion_zip", length = 20)
    private String zip;

    /** Coordenada geográfica - Latitud **/
    @Column(name = "direccion_lat", columnDefinition = "decimal(10,7)")
    private Double latitud;

    /** Coordenada geográfica - Longitud **/
    @Column(name = "direccion_lon", columnDefinition = "decimal(10,7)")
    private Double longitud;

    /**
     * Devuelve una dirección legible completa (para logs o debug).
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (calle != null && !calle.isBlank()) sb.append(calle);
        if (ciudad != null && !ciudad.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ciudad);
        }
        if (pais != null && !pais.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(pais);
        }
        return sb.toString();
    }
}