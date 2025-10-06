package co.edu.uniquindio.gohost.dto.usuarioDtos;

public record ConfirmarResetPasswordDTO(
        String token,
        String nuevaPassword
) {}
