package co.edu.uniquindio.gohost.dto.authDtos;

import co.edu.uniquindio.gohost.model.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        @NotBlank 
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
                message = "La contraseña debe contener al menos una letra minúscula, una mayúscula y un número")
        String password
) {}
