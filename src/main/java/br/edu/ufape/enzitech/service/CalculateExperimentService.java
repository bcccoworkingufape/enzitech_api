package br.edu.ufape.enzitech.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.SaveRepetitionRequestDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ProcessInfoDTO;
import br.edu.ufape.enzitech.dto.response.RepetitionResponseDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultDataDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultEnzymeDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultExperimentDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultProcessDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ExperimentTreatment;
import br.edu.ufape.enzitech.model.RepetitionStatus;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.objecthunter.exp4j.ExpressionBuilder;

@Service
@RequiredArgsConstructor
public class CalculateExperimentService {

    private final ExperimentEnzymeRepository experimentEnzymeRepository;
    private final ResultExperimentRepository resultRepository;
    private final ExperimentRepository experimentRepository;

    @Transactional(readOnly = true)
    public List<RepetitionResponseDTO> getRepetitions(UUID experimentId) {
        return resultRepository.findByExperimentId(experimentId).stream()
                .sorted(Comparator.<ResultExperiment, String>comparing(r -> r.getTreatment().getName())
                        .thenComparing(r -> r.getEnzyme().getName())
                        .thenComparing(ResultExperiment::getRepetitionNumber))
                .map(RepetitionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepetitionResponseDTO previewRepetition(UUID experimentId, SaveRepetitionRequestDTO dto) {
        ExperimentEnzyme config = findConfig(experimentId, dto.enzymeId());
        ResultExperiment slot = findSlot(experimentId, dto.treatmentId(), dto.enzymeId(), dto.repetitionNumber());

        RepetitionResult detail = calculate(config, dto.sample(), dto.whiteSample());

        return new RepetitionResponseDTO(
                slot.getId(),
                slot.getTreatment().getId(), slot.getTreatment().getName(),
                config.getId(), config.getName(),
                dto.repetitionNumber(), slot.getStatus().name(),
                dto.sample(), dto.whiteSample(),
                detail.difference(), detail.curve(), detail.finalResult()
        );
    }

    @Transactional
    public ExperimentResponseDTO saveRepetition(UUID experimentId, SaveRepetitionRequestDTO dto) {
        ExperimentEnzyme config = findConfig(experimentId, dto.enzymeId());
        ResultExperiment slot = findSlot(experimentId, dto.treatmentId(), dto.enzymeId(), dto.repetitionNumber());

        RepetitionResult detail = calculate(config, dto.sample(), dto.whiteSample());

        slot.setSample(dto.sample());
        slot.setWhiteSample(dto.whiteSample());
        slot.setDifferenceBetweenSamples(detail.difference());
        slot.setCurve(detail.curve());
        slot.setResult(detail.finalResult());
        slot.setStatus(RepetitionStatus.COMPLETED);
        resultRepository.save(slot);

        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(
                () -> new EntityNotFoundException("Experimento não encontrado."));

        long totalSlots = resultRepository.countByExperimentId(experimentId);
        long completedSlots = resultRepository.countByExperimentIdAndStatus(experimentId, RepetitionStatus.COMPLETED);
        experiment.setProgress(totalSlots > 0 ? (double) completedSlots / totalSlots : 0.0);
        experimentRepository.save(experiment);

        return ExperimentResponseDTO.fromEntity(experiment);
    }

    private ExperimentEnzyme findConfig(UUID experimentId, UUID enzymeId) {
        return experimentEnzymeRepository.findByIdAndExperimentId(enzymeId, experimentId)
                .orElseThrow(() -> new EntityNotFoundException("Configuração da enzima não encontrada para este experimento."));
    }

    private ResultExperiment findSlot(UUID experimentId, UUID treatmentId, UUID enzymeId, int repetitionNumber) {
        ResultExperiment slot = resultRepository.findByTreatmentIdAndEnzymeIdAndRepetitionNumber(treatmentId, enzymeId, repetitionNumber)
                .orElseThrow(() -> new EntityNotFoundException("Repetição não encontrada para esta combinação de tratamento e enzima."));

        if (!slot.getExperiment().getId().equals(experimentId)) {
            throw new EntityNotFoundException("Repetição não pertence a este experimento.");
        }

        return slot;
    }

    private RepetitionResult calculate(ExperimentEnzyme config, double sample, double whiteSample) {
        double difference = sample - whiteSample;
        double curve = calculateCurve(config, difference);
        double finalResult = calculateFinalResult(config, curve);
        return new RepetitionResult(difference, curve, finalResult);
    }

    private double calculateCurve(ExperimentEnzyme config, double difference) {
        boolean hasCustomCurve = config.getCustomFormulaCurve() != null && !config.getCustomFormulaCurve().isBlank();

        String formulaToUse = hasCustomCurve
                ? config.getCustomFormulaCurve()
                : config.getFormulaCurve();

        double varA = config.getVariableA() != null ? config.getVariableA() : 0.0;
        double varB = config.getVariableB() != null ? config.getVariableB() : 0.0;

        return new ExpressionBuilder(formulaToUse)
                .variables("difference", "variableA", "variableB")
                .build()
                .setVariable("difference", difference)
                .setVariable("variableA", varA)
                .setVariable("variableB", varB)
                .evaluate();
    }

    private double calculateFinalResult(ExperimentEnzyme config, double curve) {
        double duration = config.getDuration() != null && config.getDuration() > 0 ? config.getDuration() : 1.0;
        double weightSample = config.getWeightSample() != null && config.getWeightSample() > 0 ? config.getWeightSample() : 1.0;
        double size = config.getSize() != null ? config.getSize() : 0.0;
        double weightGround = config.getWeightGround() != null ? config.getWeightGround() : 0.0;

        boolean hasCustomCalculation = config.getCustomFormulaCalculation() != null && !config.getCustomFormulaCalculation().isBlank();

        String formulaToUse = hasCustomCalculation
                ? config.getCustomFormulaCalculation()
                : config.getFormulaCalculation();

        double finalResult = new ExpressionBuilder(formulaToUse)
                .variables("curve", "size", "duration", "weightSample", "weightGround")
                .build()
                .setVariable("curve", curve)
                .setVariable("size", size)
                .setVariable("duration", duration)
                .setVariable("weightSample", weightSample)
                .setVariable("weightGround", weightGround)
                .evaluate();

        if (Double.isNaN(finalResult) || Double.isInfinite(finalResult) || finalResult < 0) {
            return 0.0;
        }

        return finalResult;
    }

    @Transactional(readOnly = true)
    public List<TotalResultExperimentDTO> getTotalResult(UUID experimentId) {
        List<ResultExperiment> completedResults = resultRepository.findByExperimentId(experimentId).stream()
                .filter(r -> r.getStatus() == RepetitionStatus.COMPLETED)
                .toList();

        if (completedResults.isEmpty()) {
            return List.of();
        }

        List<TotalResultExperimentDTO> responseList = new ArrayList<>();

        Map<ExperimentEnzyme, List<ResultExperiment>> resultsByEnzyme = completedResults.stream()
                .collect(Collectors.groupingBy(ResultExperiment::getEnzyme));

        for (Map.Entry<ExperimentEnzyme, List<ResultExperiment>> enzymeEntry : resultsByEnzyme.entrySet()) {
            ExperimentEnzyme config = enzymeEntry.getKey();
            List<ResultExperiment> enzymeResults = enzymeEntry.getValue();

            TotalResultEnzymeDTO enzymeDTO = new TotalResultEnzymeDTO(
                    config.getId(), config.getName(), config.getType(),
                    config.getDescription(),
                    config.getVariableA(), config.getVariableB(),
                    config.getDuration(), config.getWeightSample(),
                    config.getWeightGround(), config.getSize()
            );

            Map<ExperimentTreatment, List<ResultExperiment>> resultsByTreatment = enzymeResults.stream()
                    .collect(Collectors.groupingBy(ResultExperiment::getTreatment));

            List<TotalResultProcessDTO> processesDTOList = new ArrayList<>();

            for (Map.Entry<ExperimentTreatment, List<ResultExperiment>> treatmentEntry : resultsByTreatment.entrySet()) {
                ExperimentTreatment treatment = treatmentEntry.getKey();
                List<ResultExperiment> treatmentResults = treatmentEntry.getValue().stream()
                        .sorted(Comparator.comparing(ResultExperiment::getRepetitionNumber))
                        .toList();

                ProcessInfoDTO processInfo = new ProcessInfoDTO(
                        treatment.getId(), treatment.getName(), treatment.getDescription()
                );

                List<TotalResultDataDTO> dataDTOList = new ArrayList<>();

                for (ResultExperiment r : treatmentResults) {
                    dataDTOList.add(new TotalResultDataDTO(
                            r.getId(),
                            r.getRepetitionNumber(),
                            r.getSample(),
                            r.getWhiteSample(),
                            r.getDifferenceBetweenSamples(),
                            config.getVariableA(),
                            config.getVariableB(),
                            r.getCurve(),
                            0.0,
                            config.getDuration().intValue(),
                            config.getSize(),
                            config.getWeightSample(),
                            r.getResult()
                    ));
                }

                processesDTOList.add(new TotalResultProcessDTO(processInfo, dataDTOList));
            }

            responseList.add(new TotalResultExperimentDTO(enzymeDTO, processesDTOList));
        }

        return responseList;
    }
}
