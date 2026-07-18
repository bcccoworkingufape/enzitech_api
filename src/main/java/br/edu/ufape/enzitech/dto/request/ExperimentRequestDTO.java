package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ExperimentRequestDTO(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Integer repetitions,
        @NotEmpty List<UUID> processes,
        @NotEmpty List<ExperimentEnzymeRequestDTO> experimentsEnzymes
) {}