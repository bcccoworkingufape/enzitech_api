package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

import br.edu.ufape.enzitech.model.ResultExperiment;

public record RepetitionResponseDTO(
        UUID id,
        UUID treatmentId,
        String treatmentName,
        UUID enzymeId,
        String enzymeName,
        Integer repetitionNumber,
        String status,
        Double sample,
        Double whiteSample,
        Double differenceBetweenSamples,
        Double curve,
        Double result
) {
    public static RepetitionResponseDTO fromEntity(ResultExperiment repetition) {
        return new RepetitionResponseDTO(
                repetition.getId(),
                repetition.getTreatment().getId(),
                repetition.getTreatment().getName(),
                repetition.getEnzyme().getId(),
                repetition.getEnzyme().getName(),
                repetition.getRepetitionNumber(),
                repetition.getStatus().name(),
                repetition.getSample(),
                repetition.getWhiteSample(),
                repetition.getDifferenceBetweenSamples(),
                repetition.getCurve(),
                repetition.getResult()
        );
    }
}
