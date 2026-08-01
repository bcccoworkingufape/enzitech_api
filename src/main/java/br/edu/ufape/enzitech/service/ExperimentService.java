package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.ExperimentEnzymeRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ExperimentTreatment;
import br.edu.ufape.enzitech.model.RepetitionStatus;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.EnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ExperimentTreatmentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final TreatmentRepository treatmentRepository;
    private final EnzymeRepository enzymeRepository;
    private final ExperimentEnzymeRepository experimentEnzymeRepository;
    private final ExperimentTreatmentRepository experimentTreatmentRepository;
    private final ResultExperimentRepository resultExperimentRepository;


    public List<Experiment> findAllByUserId(UUID userId, Boolean finished) {
        List<Experiment> experiments = experimentRepository.findByUserId(userId);

        if (finished == null) {
            return experiments;
        }

        return experiments.stream()
                .filter(experiment -> isFinished(experiment) == finished)
                .toList();
    }

    private boolean isFinished(Experiment experiment) {
        return experiment.getProgress() != null && experiment.getProgress() >= 1.0;
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
        experiment.setCreatedAt(LocalDateTime.now());
        experiment.setUpdatedAt(LocalDateTime.now());

        Experiment savedExperiment = experimentRepository.save(experiment);

        for (Treatment treatment : treatments) {
            experimentTreatmentRepository.save(buildExperimentTreatment(savedExperiment, treatment));
        }

        for (ExperimentEnzymeRequestDTO configDto : dto.experimentsEnzymes()) {
            Enzyme enzyme = enzymeRepository.findById(configDto.enzyme())
                    .orElseThrow(() -> new EntityNotFoundException("Enzima não encontrada: " + configDto.enzyme()));
            experimentEnzymeRepository.save(buildExperimentEnzyme(savedExperiment, enzyme, configDto));
        }

        reconcileRepetitionSlots(savedExperiment);
        recalculateProgress(savedExperiment);

        return experimentRepository.save(savedExperiment);
    }

    public List<ExperimentEnzyme> getEnzymesByExperiment(UUID experimentId) {
        return experimentEnzymeRepository.findByExperimentId(experimentId).stream()
                .filter(ExperimentEnzyme::getActive)
                .toList();
    }

    @Transactional
    public Experiment update(UUID id, ExperimentRequestDTO dto) {
        Experiment experiment = findById(id);
        experiment.setName(dto.name());
        experiment.setDescription(dto.description());
        experiment.setRepetitions(dto.repetitions());

        reconcileTreatments(experiment, dto.processes());
        reconcileEnzymes(experiment, dto.experimentsEnzymes());
        reconcileRepetitionSlots(experiment);
        recalculateProgress(experiment);

        return experimentRepository.save(experiment);
    }

    private void reconcileTreatments(Experiment experiment, List<UUID> desiredTreatmentIds) {
        List<ExperimentTreatment> current = experimentTreatmentRepository.findByExperimentId(experiment.getId());

        Set<UUID> activeSourceIds = new HashSet<>();

        for (ExperimentTreatment experimentTreatment : current) {
            if (!experimentTreatment.getActive()) continue;

            UUID sourceId = experimentTreatment.getSourceTreatment() != null
                    ? experimentTreatment.getSourceTreatment().getId()
                    : null;

            boolean stillSelected = sourceId != null && desiredTreatmentIds.contains(sourceId);

            if (stillSelected) {
                activeSourceIds.add(sourceId);
                continue;
            }

            boolean hasCompletedResults = resultExperimentRepository
                    .existsByTreatmentIdAndStatus(experimentTreatment.getId(), RepetitionStatus.COMPLETED);

            if (hasCompletedResults) {
                experimentTreatment.setActive(false);
                experimentTreatmentRepository.save(experimentTreatment);
            } else {
                resultExperimentRepository.deleteAll(
                        resultExperimentRepository.findByTreatmentId(experimentTreatment.getId()));
                experimentTreatmentRepository.delete(experimentTreatment);
            }
        }

        for (UUID treatmentId : desiredTreatmentIds) {
            if (activeSourceIds.contains(treatmentId)) continue;

            Treatment treatment = treatmentRepository.findById(treatmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Tratamento não encontrado: " + treatmentId));
            experimentTreatmentRepository.save(buildExperimentTreatment(experiment, treatment));
        }
    }

    private void reconcileEnzymes(Experiment experiment, List<ExperimentEnzymeRequestDTO> desiredConfigs) {
        List<ExperimentEnzyme> current = experimentEnzymeRepository.findByExperimentId(experiment.getId());

        Map<UUID, ExperimentEnzyme> activeBySourceId = new HashMap<>();
        for (ExperimentEnzyme experimentEnzyme : current) {
            if (experimentEnzyme.getActive() && experimentEnzyme.getEnzyme() != null) {
                activeBySourceId.put(experimentEnzyme.getEnzyme().getId(), experimentEnzyme);
            }
        }

        Set<UUID> desiredEnzymeIds = new HashSet<>();
        for (ExperimentEnzymeRequestDTO configDto : desiredConfigs) {
            desiredEnzymeIds.add(configDto.enzyme());
        }

        for (ExperimentEnzyme experimentEnzyme : current) {
            if (!experimentEnzyme.getActive()) continue;

            UUID sourceId = experimentEnzyme.getEnzyme() != null ? experimentEnzyme.getEnzyme().getId() : null;
            if (sourceId != null && desiredEnzymeIds.contains(sourceId)) continue;

            boolean hasCompletedResults = resultExperimentRepository
                    .existsByEnzymeIdAndStatus(experimentEnzyme.getId(), RepetitionStatus.COMPLETED);

            if (hasCompletedResults) {
                experimentEnzyme.setActive(false);
                experimentEnzymeRepository.save(experimentEnzyme);
            } else {
                resultExperimentRepository.deleteAll(
                        resultExperimentRepository.findByEnzymeId(experimentEnzyme.getId()));
                experimentEnzymeRepository.delete(experimentEnzyme);
            }
        }

        for (ExperimentEnzymeRequestDTO configDto : desiredConfigs) {
            ExperimentEnzyme existing = activeBySourceId.get(configDto.enzyme());

            if (existing != null) {
                existing.setDuration(configDto.duration());
                existing.setWeightSample(configDto.weightSample());
                existing.setWeightGround(configDto.weightGround());
                existing.setSize(configDto.size());
                if (configDto.variableA() != null) existing.setVariableA(configDto.variableA());
                if (configDto.variableB() != null) existing.setVariableB(configDto.variableB());
                experimentEnzymeRepository.save(existing);
            } else {
                Enzyme enzyme = enzymeRepository.findById(configDto.enzyme())
                        .orElseThrow(() -> new EntityNotFoundException("Enzima não encontrada: " + configDto.enzyme()));
                experimentEnzymeRepository.save(buildExperimentEnzyme(experiment, enzyme, configDto));
            }
        }
    }

    private void reconcileRepetitionSlots(Experiment experiment) {
        List<ExperimentTreatment> treatments = experimentTreatmentRepository.findByExperimentId(experiment.getId())
                .stream().filter(ExperimentTreatment::getActive).toList();
        List<ExperimentEnzyme> enzymes = experimentEnzymeRepository.findByExperimentId(experiment.getId())
                .stream().filter(ExperimentEnzyme::getActive).toList();

        int targetRepetitions = experiment.getRepetitions();

        for (ExperimentTreatment treatment : treatments) {
            for (ExperimentEnzyme enzyme : enzymes) {
                List<ResultExperiment> existingSlots = resultExperimentRepository
                        .findByExperimentIdAndTreatmentIdAndEnzymeId(experiment.getId(), treatment.getId(), enzyme.getId());

                Set<Integer> existingNumbers = new HashSet<>();
                for (ResultExperiment slot : existingSlots) {
                    existingNumbers.add(slot.getRepetitionNumber());
                }

                List<ResultExperiment> newSlots = new ArrayList<>();
                for (int repetitionNumber = 1; repetitionNumber <= targetRepetitions; repetitionNumber++) {
                    if (!existingNumbers.contains(repetitionNumber)) {
                        ResultExperiment slot = new ResultExperiment();
                        slot.setExperiment(experiment);
                        slot.setTreatment(treatment);
                        slot.setEnzyme(enzyme);
                        slot.setRepetitionNumber(repetitionNumber);
                        slot.setStatus(RepetitionStatus.PENDING);
                        newSlots.add(slot);
                    }
                }
                resultExperimentRepository.saveAll(newSlots);

                for (ResultExperiment slot : existingSlots) {
                    boolean exceedsTarget = slot.getRepetitionNumber() > targetRepetitions;
                    if (exceedsTarget && slot.getStatus() == RepetitionStatus.PENDING) {
                        resultExperimentRepository.delete(slot);
                    }
                }
            }
        }
    }

    private void recalculateProgress(Experiment experiment) {
        long total = resultExperimentRepository.countByExperimentId(experiment.getId());
        long completed = resultExperimentRepository.countByExperimentIdAndStatus(experiment.getId(), RepetitionStatus.COMPLETED);
        experiment.setProgress(total > 0 ? (double) completed / total : 0.0);
    }

    private ExperimentTreatment buildExperimentTreatment(Experiment experiment, Treatment treatment) {
        ExperimentTreatment experimentTreatment = new ExperimentTreatment();
        experimentTreatment.setExperiment(experiment);
        experimentTreatment.setSourceTreatment(treatment);
        experimentTreatment.setName(treatment.getName());
        experimentTreatment.setDescription(treatment.getDescription());
        experimentTreatment.setActive(true);
        return experimentTreatment;
    }

    private ExperimentEnzyme buildExperimentEnzyme(Experiment experiment, Enzyme enzyme, ExperimentEnzymeRequestDTO configDto) {
        ExperimentEnzyme config = new ExperimentEnzyme();
        config.setExperiment(experiment);
        config.setEnzyme(enzyme);
        config.setName(enzyme.getName());
        config.setType(enzyme.getType());
        config.setDescription(enzyme.getDescription());
        config.setFormulaCurve(enzyme.getFormulaCurve());
        config.setFormulaCalculation(enzyme.getFormulaCalculation());
        config.setDuration(configDto.duration());
        config.setWeightSample(configDto.weightSample());
        config.setWeightGround(configDto.weightGround());
        config.setSize(configDto.size());
        config.setVariableA(configDto.variableA() != null ? configDto.variableA() : enzyme.getVariableA());
        config.setVariableB(configDto.variableB() != null ? configDto.variableB() : enzyme.getVariableB());
        config.setActive(true);
        return config;
    }

    @Transactional
    public void delete(UUID id) {
        Experiment experiment = findById(id);
        experimentRepository.delete(experiment);
    }
}
