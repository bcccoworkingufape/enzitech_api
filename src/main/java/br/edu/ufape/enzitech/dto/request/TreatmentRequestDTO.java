package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TreatmentRequestDTO(
        
        @NotBlank(message = "O nome do tratamento é obrigatório.")
        String name,
        
        String description,
        
        @NotNull(message = "O ID do experimento não pode ser nulo.")
        UUID experimentId
) {}