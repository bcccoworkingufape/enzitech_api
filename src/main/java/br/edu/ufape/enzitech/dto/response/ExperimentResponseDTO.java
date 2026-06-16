package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Experiment;
import java.util.UUID;

public record ExperimentResponseDTO(
        UUID id,
        String name,
        String description,
        UserResponseDTO user
) {
    public static ExperimentResponseDTO fromEntity(Experiment experiment) {
        return new ExperimentResponseDTO(
                experiment.getId(),
                experiment.getName(),
                experiment.getDescription(),
                UserResponseDTO.fromEntity(experiment.getUser())
        );
    }
}