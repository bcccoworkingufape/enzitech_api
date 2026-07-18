package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.EnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final TreatmentRepository treatmentRepository;
    private final EnzymeRepository enzymeRepository;
    private final ExperimentEnzymeRepository experimentEnzymeRepository;
    private final ResultExperimentRepository resultExperimentRepository;


    public List<Experiment> findAllByUserId(UUID userId) {
        return experimentRepository.findByUserId(userId);
    }

    public Experiment findById(UUID id) {
        return experimentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Experimento não encontrado."));
    }

    @Transactional
    public Experiment create(ExperimentRequestDTO dto, User user) {
        List<Treatment> treatments = treatmentRepository.findAllById(dto.processes());
        if (treatments.isEmpty()) {
            throw new RuntimeException("Nenhum tratamento (processo) válido encontrado.");
        }

        Experiment experiment = new Experiment();
        experiment.setName(dto.name());
        experiment.setDescription(dto.description());
        experiment.setRepetitions(dto.repetitions());
        experiment.setProgress(0.0);
        experiment.setUser(user);
        experiment.setProcesses(treatments);
        experiment.setCreatedAt(LocalDateTime.now());
        experiment.setUpdatedAt(LocalDateTime.now());
        
        Experiment savedExperiment = experimentRepository.save(experiment);

        List<ExperimentEnzyme> experimentEnzymes = dto.experimentsEnzymes().stream().map(configDto -> {
            Enzyme enzyme = enzymeRepository.findById(configDto.enzyme())
                    .orElseThrow(() -> new RuntimeException("Enzima não encontrada"));

            ExperimentEnzyme config = new ExperimentEnzyme();
            config.setExperiment(savedExperiment);
            config.setEnzyme(enzyme);
            config.setDuration(configDto.duration());
            config.setWeightSample(configDto.weightSample());
            config.setWeightGround(configDto.weightGround());
            config.setSize(configDto.size());
            config.setVariableA(configDto.variableA() != null ? configDto.variableA() : enzyme.getVariableA());
            config.setVariableB(configDto.variableB() != null ? configDto.variableB() : enzyme.getVariableB());

            return config;
        }).toList();

        experimentEnzymeRepository.saveAll(experimentEnzymes);

        return savedExperiment;
    }

    public List<Enzyme> getRemainingEnzymes(UUID experimentId, UUID treatmentId) {
        List<Enzyme> allConfiguredEnzymes = experimentEnzymeRepository.findByExperimentId(experimentId)
                .stream()
                .map(ExperimentEnzyme::getEnzyme)
                .toList();

        List<UUID> calculatedIds = resultExperimentRepository.findCalculatedEnzymeIds(experimentId, treatmentId);

        return allConfiguredEnzymes.stream()
                .filter(enzyme -> !calculatedIds.contains(enzyme.getId()))
                .toList();
    }

    public List<Enzyme> getEnzymesByExperiment(UUID experimentId) {
        return experimentEnzymeRepository.findByExperimentId(experimentId)
                .stream()
                .map(ExperimentEnzyme::getEnzyme)
                .toList();
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