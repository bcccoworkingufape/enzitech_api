package br.edu.ufape.enzitech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CalculateExperimentRequestDTO(
        
        @Schema(description = "ID da enzima", example = "25a243db-1008-4268-a08b-efb7429f6bfa")
        @NotNull(message = "O ID da enzima é obrigatório.")
        UUID enzymeId,

        @Schema(description = "ID do tratamento (antigo process)", example = "a1b2c3d4-1008-4268-a08b-efb7429f6bfa")
        @NotNull(message = "O ID do tratamento é obrigatório.")
        UUID treatmentId,

        @Schema(description = "Lista de dados para calcular o resultado do experimento")
        @NotEmpty(message = "A lista de dados do experimento não pode estar vazia.")
        @Valid // Valida cada item dentro da lista
        List<ExperimentDataRequestDTO> experimentData
) {}