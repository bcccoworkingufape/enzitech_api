package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.TreatmentRequestDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final ResultExperimentRepository resultExperimentRepository;
    private final ExperimentRepository experimentRepository;

    public Treatment findById(UUID id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamento não encontrado com o id: " + id));
    }

    public List<Treatment> findByExperiment(UUID experimentId) {
        return treatmentRepository.findByExperimentId(experimentId);
    }
    
    public List<Treatment> findByUser(UUID userId) {
        return treatmentRepository.findByUserId(userId); 
    }

    @Transactional
    public Treatment create(TreatmentRequestDTO dto, User user) {
        Treatment treatment = new Treatment();
        treatment.setName(dto.name());
        treatment.setDescription(dto.description());
        treatment.setUser(user);

        return treatmentRepository.save(treatment);
    }

    @Transactional
    public Treatment save(Treatment treatment) {
        if (treatment.getExperiment() != null && treatment.getExperiment().getId() != null) {
            Experiment experiment = experimentRepository.findById(treatment.getExperiment().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Experimento vinculado não encontrado."));
            treatment.setExperiment(experiment);
        }

        return treatmentRepository.save(treatment);
    }

    @Transactional
    public void delete(UUID id) {
        boolean hasResults = resultExperimentRepository.existsByTreatmentId(id);
        
        if (hasResults) {
            throw new IllegalStateException("Não é possível eliminar este tratamento pois já existem resultados de cálculos associados a ele.");
        }

        Treatment treatment = findById(id);
        treatmentRepository.delete(treatment);
    }
}