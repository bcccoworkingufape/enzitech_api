package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentRepository experimentRepository;

    public Page<Experiment> findAllByUserId(UUID userId, Pageable pageable) {
        return experimentRepository.findByUserId(userId, pageable);
    }

    public Experiment findById(UUID id) {
        return experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Experimento não encontrado."));
    }

    @Transactional
    public Experiment create(ExperimentRequestDTO dto, User user) {
        Experiment experiment = new Experiment();
        experiment.setName(dto.name());
        experiment.setDescription(dto.description());
        experiment.setUser(user);
        experiment.setCreatedAt(LocalDateTime.now());
        experiment.setUpdatedAt(LocalDateTime.now());
        return experimentRepository.save(experiment);
    }

    @Transactional
    public Experiment update(UUID id, ExperimentRequestDTO dto) {
        Experiment experiment = findById(id);
        experiment.setName(dto.name());
        experiment.setDescription(dto.description());
        return experimentRepository.save(experiment);
    }

    @Transactional
    public void delete(UUID id) {
        Experiment experiment = findById(id);
        experimentRepository.delete(experiment);
    }
}