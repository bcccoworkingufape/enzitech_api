package br.edu.ufape.enzitech.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ExperimentDataRequestDTO(
        
        @Schema(description = "Amostra do experimento", example = "0.405")
        @NotNull(message = "O valor da amostra não pode ser nulo.")
        Double sample,

        @Schema(description = "Amostra em branco do experimento", example = "0.39")
        @NotNull(message = "O valor da amostra em branco não pode ser nulo.")
        Double whiteSample
) {}