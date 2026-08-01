package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.ExperimentTreatment;
import br.edu.ufape.enzitech.model.Treatment;

import java.time.LocalDateTime;
import java.util.UUID;

public record TreatmentResponseDTO(
        UUID id,
        UUID sourceTreatmentId,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TreatmentResponseDTO fromEntity(Treatment treatment) {
        return new TreatmentResponseDTO(
                treatment.getId(),
                treatment.getId(),
                treatment.getName(),
                treatment.getDescription(),
                treatment.getCreatedAt(),
                treatment.getUpdatedAt()
        );
    }

    public static TreatmentResponseDTO fromExperimentTreatment(ExperimentTreatment experimentTreatment) {
        return new TreatmentResponseDTO(
                experimentTreatment.getId(),
                experimentTreatment.getSourceTreatment() != null ? experimentTreatment.getSourceTreatment().getId() : null,
                experimentTreatment.getName(),
                experimentTreatment.getDescription(),
                experimentTreatment.getCreatedAt(),
                experimentTreatment.getUpdatedAt()
        );
    }
}