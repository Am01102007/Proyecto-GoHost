
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
}
