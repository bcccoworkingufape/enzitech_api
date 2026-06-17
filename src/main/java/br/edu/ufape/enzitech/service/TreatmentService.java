package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final ResultExperimentRepository resultExperimentRepository;

    public Treatment findById(UUID id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamento não encontrado."));
    }

    public List<Treatment> findByExperiment(UUID experimentId) {
        return treatmentRepository.findByExperimentId(experimentId);
    }

    @Transactional
    public Treatment save(Treatment treatment) {
        treatment.setCreatedAt(LocalDateTime.now());
        treatment.setUpdatedAt(LocalDateTime.now());
        return treatmentRepository.save(treatment);
    }

    @Transactional
    public void delete(UUID id) {
        boolean hasResults = resultExperimentRepository.existsByTreatmentId(id);
        
        if (hasResults) {
            throw new IllegalStateException("Não é possível eliminar este tratamento pois já existem resultados de cálculos associados a ele.");
        }

        Treatment treatment = findById(id);
        treatmentRepository.delete(treatment); //Aqui o Hibernate dá Soft Delete
    }
}