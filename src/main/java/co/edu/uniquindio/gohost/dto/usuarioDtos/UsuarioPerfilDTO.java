package co.edu.uniquindio.gohost.dto.usuarioDtos;

import co.edu.uniquindio.gohost.model.Rol;
import co.edu.uniquindio.gohost.model.TipoDocumento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para retornar datos del perfil del usuario autenticado.
 * Excluye campos sensibles como contraseña y coordenadas exactas.
 */
public record UsuarioPerfilDTO(
        UUID id,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String email,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String telefono,
        String ciudad,
        String pais,
        String direccion,
        String fotoPerfil,
        Rol rol,
        boolean activo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {}