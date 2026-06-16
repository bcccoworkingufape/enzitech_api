package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExperimentRequestDTO(
        @NotBlank(message = "O nome do experimento é obrigatório.")
        String name,
        
        @NotBlank(message = "A descrição do experimento é obrigatória.")
        String description
) {}