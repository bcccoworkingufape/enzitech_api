package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDTO(
        @NotBlank String email,
        @NotBlank String token,
        @NotBlank String newPassword
) {}