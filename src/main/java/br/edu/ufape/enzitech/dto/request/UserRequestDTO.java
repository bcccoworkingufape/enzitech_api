package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,
        
        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "O formato do email é inválido.")
        String email,
        
        @NotBlank(message = "A palavra-passe é obrigatória.")
        String password
) {}
