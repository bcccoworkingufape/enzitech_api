package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Enzyme;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnzymeResponseDTO(
        UUID id,
        String name,
        Double variableA,
        Double variableB,
        String type,
        String formula, 
        String formulaCurve,
        String formulaCalculation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EnzymeResponseDTO fromEntity(Enzyme enzyme) {
        return new EnzymeResponseDTO(
                enzyme.getId(),
                enzyme.getName(),
                enzyme.getVariableA(),
                enzyme.getVariableB(),
                enzyme.getType(),
                enzyme.getDescription(),
                enzyme.getFormulaCurve(),
                enzyme.getFormulaCalculation(),
                enzyme.getCreatedAt(),
                enzyme.getUpdatedAt()
        );
    }
}