package co.edu.uniquindio.gohost.dto;

import co.edu.uniquindio.gohost.model.TipoDocumento;

import java.time.LocalDate; /** Editar usuario **/
public record EditarUsuarioDTO(
        String nombre,
        String apellidos,
        String telefono,
        String ciudad,
        String pais,
        LocalDate fechaNacimiento,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String email
) {}
