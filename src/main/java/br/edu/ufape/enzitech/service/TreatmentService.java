package br.edu.ufape.enzitech.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.TreatmentRequestDTO;
import br.edu.ufape.enzitech.model.ExperimentTreatment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.ExperimentTreatmentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final ExperimentTreatmentRepository experimentTreatmentRepository;


    public Treatment findById(UUID id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamento não encontrado com o id: " + id));
    }

    public List<ExperimentTreatment> findByExperiment(UUID experimentId) {
        return experimentTreatmentRepository.findByExperimentId(experimentId);
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

        Treatment saved = treatmentRepository.save(treatment);
        log.info("Tratamento criado: id={}, nome={}, usuario={}", saved.getId(), saved.getName(), user.getEmail());
        return saved;
    }
    @Transactional
    public Treatment save(Treatment treatment) {
        return treatmentRepository.save(treatment);
    }

    @Transactional
    public void delete(UUID id) {
        Treatment treatment = findById(id);
        log.info("Tratamento removido: id={}, nome={}", treatment.getId(), treatment.getName());
        treatmentRepository.delete(treatment);
    }
}