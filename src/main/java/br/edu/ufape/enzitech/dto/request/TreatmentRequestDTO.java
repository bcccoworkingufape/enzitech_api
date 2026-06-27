package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TreatmentRequestDTO(
        
        @NotBlank(message = "O nome do tratamento é obrigatório.")
        String name,
        
        String description
) {}