
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.Embeddable;
import lombok.*;

/** Dirección postal embebida en Alojamiento **/
@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Direccion {
    /** Ciudad donde se ubica el alojamiento **/ private String ciudad;
    /** País de residencia (ISO-2) **/ private String pais;
    /** Calle y número **/ private String calle;
    /** Código postal **/ private String zip;

    private Double latitud;
    private Double longitud;

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
