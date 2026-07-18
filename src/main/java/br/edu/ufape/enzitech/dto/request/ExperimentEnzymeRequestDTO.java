package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ExperimentEnzymeRequestDTO(
        @NotNull UUID enzyme,
        Double variableA,
        Double variableB,
        @NotNull Double duration,
        @NotNull Double weightSample,
        @NotNull Double weightGround,
        @NotNull Double size
) {}