package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

public record TotalResultEnzymeDTO(
        UUID id,
        String name,
        String type,
        String formula,
        Double variableA,
        Double variableB,
        Double duration,
        Double weightSample,
        Double weightGround,
        Double size
) {}
