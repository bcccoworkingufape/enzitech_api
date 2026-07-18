package br.edu.ufape.enzitech.dto.request;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CalculateExperimentRequestDTO(
        
        @NotNull(message = "O ID do tratamento é obrigatório.")
        @JsonAlias({"process", "treatment", "processId", "treatmentId"})
        UUID treatmentId,

        @NotNull(message = "O ID da enzima é obrigatório.")
        @JsonAlias({"enzyme", "enzymeId"})
        UUID enzymeId,

        @NotEmpty(message = "Os dados do experimento não podem estar vazios.")
        List<@Valid ExperimentDataRequestDTO> experimentData 
) {}