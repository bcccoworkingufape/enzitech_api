package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Treatment;
import java.util.UUID;

public record TreatmentResponseDTO(
        UUID id,
        String name,
        String description,
        UUID experimentId
) {
    
    public static TreatmentResponseDTO fromEntity(Treatment treatment) {
        return new TreatmentResponseDTO(
                treatment.getId(),
                treatment.getName(),
                treatment.getDescription(),
                treatment.getExperiment().getId()
        );
    }
}