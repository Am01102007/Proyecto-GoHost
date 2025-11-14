package co.edu.uniquindio.gohost.dto.mensajeDtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensajeResDTO(
        UUID id,
        UUID reservaId,
        UUID remitenteId,
        String remitenteNombre,
        String contenido,
        LocalDateTime creadoEn
) {}

