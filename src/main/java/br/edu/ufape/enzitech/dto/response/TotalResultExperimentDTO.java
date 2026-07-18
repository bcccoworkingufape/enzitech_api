package br.edu.ufape.enzitech.dto.response;

import java.util.List;

public record TotalResultExperimentDTO(
        TotalResultEnzymeDTO enzyme,
        List<TotalResultProcessDTO> processes
) {}
