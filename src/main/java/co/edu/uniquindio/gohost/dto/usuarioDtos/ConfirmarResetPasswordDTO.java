package co.edu.uniquindio.gohost.dto.usuarioDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmarResetPasswordDTO(
        @NotBlank String token,
        @NotBlank 
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
                message = "La contraseña debe contener al menos una letra minúscula, una mayúscula y un número")
        String nuevaPassword
) {}
