package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExperimentEnzymeResponseDTO(
        UUID id,
        EnzymeResponseDTO enzyme,
        Double variableA,
        Double variableB,
        Double duration,
        Double weightSample,
        Double weightGround,
        Double size,
        String customFormulaCurve,
        String customFormulaCalculation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExperimentEnzymeResponseDTO fromEntity(ExperimentEnzyme config) {
        return new ExperimentEnzymeResponseDTO(
                config.getId(),
                EnzymeResponseDTO.fromExperimentEnzyme(config),
                config.getVariableA(),
                config.getVariableB(),
                config.getDuration(),
                config.getWeightSample(),
                config.getWeightGround(),
                config.getSize(),
                config.getCustomFormulaCurve(),
                config.getCustomFormulaCalculation(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}