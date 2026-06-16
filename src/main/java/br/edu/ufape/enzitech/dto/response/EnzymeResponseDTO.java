package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Enzyme;
import java.util.UUID;

public record EnzymeResponseDTO(
        UUID id,
        String name,
        String type,
        String description,
        String formulaCurve,
        String formulaCalculation
) {
    public static EnzymeResponseDTO fromEntity(Enzyme enzyme) {
        return new EnzymeResponseDTO(
                enzyme.getId(),
                enzyme.getName(),
                enzyme.getType(),
                enzyme.getDescription(),
                enzyme.getFormulaCurve(),
                enzyme.getFormulaCalculation()
        );
    }
}