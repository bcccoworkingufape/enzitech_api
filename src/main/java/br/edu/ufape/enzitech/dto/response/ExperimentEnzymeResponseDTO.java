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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExperimentEnzymeResponseDTO fromEntity(ExperimentEnzyme config) {
        return new ExperimentEnzymeResponseDTO(
                config.getId(),
                config.getEnzyme() != null ? EnzymeResponseDTO.fromEntity(config.getEnzyme()) : null,
                config.getVariableA(),
                config.getVariableB(),
                config.getDuration(),
                config.getWeightSample(),
                config.getWeightGround(),
                config.getSize(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}