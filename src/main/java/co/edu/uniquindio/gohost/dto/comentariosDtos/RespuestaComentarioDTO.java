package co.edu.uniquindio.gohost.dto.comentariosDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID; /** Responder comentario **/
public record RespuestaComentarioDTO(
        @NotBlank String respuesta,
        @NotNull UUID anfitrionId) {}
