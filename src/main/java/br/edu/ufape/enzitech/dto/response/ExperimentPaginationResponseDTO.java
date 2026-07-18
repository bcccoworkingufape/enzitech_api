package br.edu.ufape.enzitech.dto.response;

import java.util.List;

public record ExperimentPaginationResponseDTO(
        long total,
        List<ExperimentResponseDTO> experiments
) {}