package co.edu.uniquindio.gohost.dto.authDtos;

import java.util.UUID; /** Token **/
public record TokenDTO(String token, UUID usuarioId, String rol) {}
