package br.edu.ufape.enzitech.dto.response;

import java.util.List;

public record CalculateExperimentResponseDTO(
        List<Double> results,
        Double average
) {}