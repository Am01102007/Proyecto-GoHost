package co.edu.uniquindio.gohost.dto.usuarioDtos;

import co.edu.uniquindio.gohost.model.Rol;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para usuario.
 * Excluye información sensible como contraseñas y tokens.
 * Se usa en listados y consultas públicas de usuarios.
 */
public record UsuarioResDTO(
        UUID id,
        String nombre,
        String email,
        String telefono,
        String ciudad,
        String pais,
        Rol rol,
        Boolean activo,
        LocalDateTime fechaRegistro
) {}