package br.edu.ufape.enzitech.dto.response;

import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ExperimentTreatment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExperimentResponseDTO(
        UUID id,
        String name,
        String description,
        Integer repetitions,
        Double progress,
        UserResponseDTO user,
        List<TreatmentResponseDTO> processes,
        List<ExperimentEnzymeResponseDTO> experimentEnzymes, 
        List<EnzymeResponseDTO> enzymes, 
        
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExperimentResponseDTO fromEntity(Experiment experiment) {
        return new ExperimentResponseDTO(
                experiment.getId(),
                experiment.getName(),
                experiment.getDescription(),
                experiment.getRepetitions() != null ? experiment.getRepetitions() : 0,
                experiment.getProgress() != null ? experiment.getProgress() : 0.0,
                UserResponseDTO.fromEntity(experiment.getUser()),
                experiment.getExperimentTreatments() != null ?
                        experiment.getExperimentTreatments().stream()
                                .filter(ExperimentTreatment::getActive)
                                .map(TreatmentResponseDTO::fromExperimentTreatment).toList() :
                        List.of(),
                experiment.getExperimentEnzymes() != null ?
                        experiment.getExperimentEnzymes().stream()
                                .filter(ExperimentEnzyme::getActive)
                                .map(ExperimentEnzymeResponseDTO::fromEntity).toList() :
                        List.of(),
                experiment.getExperimentEnzymes() != null ?
                        experiment.getExperimentEnzymes().stream()
                                .filter(ExperimentEnzyme::getActive)
                                .map(EnzymeResponseDTO::fromExperimentEnzyme)
                                .toList() :
                        List.of(),
                experiment.getCreatedAt(),
                experiment.getUpdatedAt()
        );
    }
}