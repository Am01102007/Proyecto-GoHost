package co.edu.uniquindio.gohost.dto;

import co.edu.uniquindio.gohost.model.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate; /** Registro **/
public record RegistroDTO(
        @Email @NotBlank String email,
        @NotBlank String nombre,
        String apellidos,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        LocalDate fechaNacimiento,
        String telefono,
        String ciudad,
        String pais,
        @NotBlank String password
) {}
