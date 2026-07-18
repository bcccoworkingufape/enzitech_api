package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

public record TotalResultDataDTO(
        UUID id, 
        Integer repetitionId, 
        Double sample,
        Double whiteSample,
        Double differenceBetweenSamples,
        Double variableA,
        Double variableB,
        Double curve,
        Double correctionFactor, 
        Integer time,             
        Double volume,           
        Double weightSample,
        Double result
) {}