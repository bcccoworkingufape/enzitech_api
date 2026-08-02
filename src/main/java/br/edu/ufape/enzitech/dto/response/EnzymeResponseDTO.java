package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnzymeResponseDTO(
        UUID id,
        UUID sourceEnzymeId,
        String name,
        Double variableA,
        Double variableB,
        String type,
        String formula,
        String formulaCurve,
        String formulaCalculation,
        String customFormulaCurve,
        String customFormulaCalculation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EnzymeResponseDTO fromEntity(Enzyme enzyme) {
        return new EnzymeResponseDTO(
                enzyme.getId(),
                enzyme.getId(),
                enzyme.getName(),
                enzyme.getVariableA(),
                enzyme.getVariableB(),
                enzyme.getType(),
                enzyme.getDescription(),
                enzyme.getFormulaCurve(),
                enzyme.getFormulaCalculation(),
                null,
                null,
                enzyme.getCreatedAt(),
                enzyme.getUpdatedAt()
        );
    }

    public static EnzymeResponseDTO fromExperimentEnzyme(ExperimentEnzyme config) {
        return new EnzymeResponseDTO(
                config.getId(),
                config.getEnzyme() != null ? config.getEnzyme().getId() : null,
                config.getName(),
                config.getVariableA(),
                config.getVariableB(),
                config.getType(),
                config.getDescription(),
                config.getFormulaCurve(),
                config.getFormulaCalculation(),
                config.getCustomFormulaCurve(),
                config.getCustomFormulaCalculation(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}