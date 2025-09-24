
/*
 * Direccion — Objeto de valor embebido (@Embeddable)
 * no tiene tabla propia sus campos viven dentro de la tabla 'usuarios'.
 */
package co.edu.uniquindio.gohost.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;

@Embeddable @Getter @Setter
public class Direccion {
    //línea 1 (calle principal, número y/o interior)
    @Size(max=120) private String linea1;
    //línea 2 (complemento: torre, apto, etc.)
    @Size(max=120) private String linea2;
    //ciudad de residencia
    @Size(max=80)  private String ciudad;
    //departamento/estado/provincia
    @Size(max=80)  private String departamento;
    //país
    @Size(max=80)  private String pais;
    //ZIP/CP si aplica
    @Size(max=20)  private String codigoPostal;
}

