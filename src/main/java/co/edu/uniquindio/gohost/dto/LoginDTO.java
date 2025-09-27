package co.edu.uniquindio.gohost.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank; /** Login **/ public record LoginDTO(@Email @NotBlank String email, @NotBlank String password) {}
