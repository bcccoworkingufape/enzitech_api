package br.edu.ufape.enzitech.dto.response;

import java.util.List;

public record ExperimentResultWrapperDTO(
        List<TotalResultExperimentDTO> result 
) {}