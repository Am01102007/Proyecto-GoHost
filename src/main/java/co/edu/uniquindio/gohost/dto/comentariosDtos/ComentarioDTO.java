package co.edu.uniquindio.gohost.dto.comentariosDtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID; /** Crear comentario **/
public record ComentarioDTO(
        @NotBlank String texto,
        @Min(1) @Max(5) int calificacion,
        @NotNull UUID autorId) {

}
