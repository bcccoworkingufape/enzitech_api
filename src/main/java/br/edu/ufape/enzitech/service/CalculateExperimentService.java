package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.CalculateExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentDataRequestDTO;
import br.edu.ufape.enzitech.dto.response.CalculateExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ProcessInfoDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultDataDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultEnzymeDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultExperimentDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultProcessDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@Service
@RequiredArgsConstructor
public class CalculateExperimentService {

    private final ExperimentEnzymeRepository experimentEnzymeRepository;
    private final ResultExperimentRepository resultRepository;
    private final TreatmentService treatmentService;
    private final ExperimentRepository experimentRepository;
    private final TreatmentRepository treatmentRepository;


public CalculateExperimentResponseDTO calculatePreview(UUID experimentId, CalculateExperimentRequestDTO dto) {
        ExperimentEnzyme config = experimentEnzymeRepository.findByExperimentIdAndEnzymeId(experimentId, dto.enzymeId())
                .orElseThrow();
        
        List<RepetitionResult> details = performCalculations(config, dto.experimentData());

        List<Double> results = details.stream().map(RepetitionResult::finalResult).toList();
        double average = results.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        return new CalculateExperimentResponseDTO(results, average);
    }

    @Transactional
    public void saveResults(UUID experimentId, CalculateExperimentRequestDTO dto) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow();
        Treatment treatment = treatmentRepository.findById(dto.treatmentId()).orElseThrow();
        ExperimentEnzyme config = experimentEnzymeRepository.findByExperimentIdAndEnzymeId(experimentId, dto.enzymeId())
                .orElseThrow();

        List<RepetitionResult> details = performCalculations(config, dto.experimentData());

        /* Remove os resultados anteriores desta combinação específica antes de salvar os novos
        List<ResultExperiment> oldResults = resultRepository.findByExperimentIdAndTreatmentIdAndEnzymeId(experimentId, dto.treatmentId(), dto.enzymeId());
        if (!oldResults.isEmpty()) {
            resultRepository.deleteAll(oldResults);
        }
        */
        
        for (int i = 0; i < details.size(); i++) {
            RepetitionResult detail = details.get(i);
            
            ResultExperiment resultExperiment = new ResultExperiment();
            resultExperiment.setExperiment(experiment);
            resultExperiment.setTreatment(treatment);
            resultExperiment.setEnzyme(config.getEnzyme());
            resultExperiment.setSample(dto.experimentData().get(i).sample());
            resultExperiment.setWhiteSample(dto.experimentData().get(i).whiteSample());
            resultExperiment.setDifferenceBetweenSamples(detail.difference());
            resultExperiment.setCurve(detail.curve());
            resultExperiment.setResult(detail.finalResult());
        
            resultExperiment.setCreatedAt(LocalDateTime.now());
            resultExperiment.setUpdatedAt(LocalDateTime.now());
            
            resultRepository.save(resultExperiment);
        }

        resultRepository.flush(); 

        int totalTreatments = experiment.getProcesses().size();
        int totalEnzymes = experiment.getExperimentEnzymes().size();
        int totalCombinations = totalTreatments * totalEnzymes;

        if (totalCombinations > 0) {
            List<ResultExperiment> allResults = resultRepository.findByExperimentId(experimentId);

            long completedCombinations = allResults.stream()
                    .map(r -> r.getTreatment().getId().toString() + "-" + r.getEnzyme().getId().toString())
                    .distinct()
                    .count();

            double newProgress = (double) completedCombinations / totalCombinations;

            System.out.println("=== ATUALIZAÇÃO DE PROGRESSO ===");
            System.out.println("Tratamentos: " + totalTreatments + " | Enzimas: " + totalEnzymes);
            System.out.println("Combinações concluídas: " + completedCombinations + " de " + totalCombinations);
            System.out.println("Progresso salvo no banco: " + newProgress);

            experiment.setProgress(newProgress);
            experimentRepository.save(experiment);
        }
    }

    @Transactional
    public List<ResultExperiment> calculateAndSaveResults(UUID experimentId, CalculateExperimentRequestDTO dto) {
        ExperimentEnzyme config = experimentEnzymeRepository.findByExperimentIdAndEnzymeId(experimentId, dto.enzymeId())
                .orElseThrow(() -> new RuntimeException("Configuração da Enzima não encontrada para este Experimento."));

        Treatment treatment = treatmentService.findById(dto.treatmentId());
        Enzyme enzyme = config.getEnzyme();

        List<ResultExperiment> savedResults = new ArrayList<>();

        for (ExperimentDataRequestDTO data : dto.experimentData()) {

            double difference = data.sample() - data.whiteSample();
            double curve = calculateCurve(config, difference);
            double finalResult = calculateFinalResult(enzyme, config, curve);

            ResultExperiment result = new ResultExperiment();
            result.setExperiment(config.getExperiment());
            result.setEnzyme(enzyme);
            result.setTreatment(treatment);
            result.setSample(data.sample());
            result.setWhiteSample(data.whiteSample());
            result.setDifferenceBetweenSamples(difference);
            result.setCurve(curve);
            result.setResult(finalResult);
            result.setCreatedAt(LocalDateTime.now());
            result.setUpdatedAt(LocalDateTime.now());

            savedResults.add(result);
        }

        return resultRepository.saveAll(savedResults);
    }

    private List<RepetitionResult> performCalculations(ExperimentEnzyme config, List<ExperimentDataRequestDTO> dataList) {
        List<RepetitionResult> results = new ArrayList<>();
        Enzyme enzyme = config.getEnzyme(); 

        for (ExperimentDataRequestDTO data : dataList) {

            double difference = data.sample() - data.whiteSample();

            Expression curveExp = new ExpressionBuilder(enzyme.getFormulaCurve())
                    .variables("difference", "variableA", "variableB")
                    .build()
                    .setVariable("difference", difference)
                    .setVariable("variableA", config.getVariableA())
                    .setVariable("variableB", config.getVariableB());
            double curve = curveExp.evaluate();

            Expression resultExp = new ExpressionBuilder(enzyme.getFormulaCalculation())
                    .variables("curve", "size", "duration", "weightSample", "weightGround")
                    .build()
                    .setVariable("curve", curve)
                    .setVariable("size", config.getSize())
                    .setVariable("duration", config.getDuration())
                    .setVariable("weightSample", config.getWeightSample())
                    .setVariable("weightGround", config.getWeightGround());
            double finalResult = resultExp.evaluate();

            if (Double.isNaN(finalResult) || Double.isInfinite(finalResult)) {
                finalResult = 0.0;
            } else if (finalResult < 0) {
                finalResult = 0.0;
            }

            results.add(new RepetitionResult(difference, curve, finalResult));
        }

        return results;
    }

    private double calculateCurve(ExperimentEnzyme config, double difference) {
        boolean hasCustomCurve = config.getCustomFormulaCurve() != null && !config.getCustomFormulaCurve().isBlank();
        
        String formulaToUse = hasCustomCurve 
                ? config.getCustomFormulaCurve() 
                : config.getEnzyme().getFormulaCurve();

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

    private double calculateFinalResult(Enzyme enzyme, ExperimentEnzyme config, double curve) {
        double duration = config.getDuration() != null && config.getDuration() > 0 ? config.getDuration() : 1.0;
        double weightSample = config.getWeightSample() != null && config.getWeightSample() > 0 ? config.getWeightSample() : 1.0;
        double size = config.getSize() != null ? config.getSize() : 0.0;
        double weightGround = config.getWeightGround() != null ? config.getWeightGround() : 0.0;

        return new ExpressionBuilder(enzyme.getFormulaCalculation())
                .variables("curve", "size", "duration", "weightSample", "weightGround")
                .build()
                .setVariable("curve", curve)
                .setVariable("size", size)
                .setVariable("duration", duration)
                .setVariable("weightSample", weightSample)
                .setVariable("weightGround", weightGround)
                .evaluate();
    }

    @Transactional(readOnly = true)
    public List<TotalResultExperimentDTO> getTotalResult(UUID experimentId) {
        List<ResultExperiment> allResults = resultRepository.findByExperimentId(experimentId);
        
        if (allResults.isEmpty()) {
            return List.of();
        }
        
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow();
        List<TotalResultExperimentDTO> responseList = new ArrayList<>();

        Map<Enzyme, List<ResultExperiment>> resultsByEnzyme = allResults.stream()
                .collect(Collectors.groupingBy(ResultExperiment::getEnzyme));

        for (Map.Entry<Enzyme, List<ResultExperiment>> enzymeEntry : resultsByEnzyme.entrySet()) {
            Enzyme enzyme = enzymeEntry.getKey();
            List<ResultExperiment> enzymeResults = enzymeEntry.getValue();

            ExperimentEnzyme config = experiment.getExperimentEnzymes().stream()
                    .filter(ee -> ee.getEnzyme().getId().equals(enzyme.getId()))
                    .findFirst()
                    .orElseThrow();

            TotalResultEnzymeDTO enzymeDTO = new TotalResultEnzymeDTO(
                    enzyme.getId(), enzyme.getName(), enzyme.getType(),
                    enzyme.getDescription(),
                    config.getVariableA(), config.getVariableB(),
                    config.getDuration(), config.getWeightSample(),
                    config.getWeightGround(), config.getSize()
            );

            Map<Treatment, List<ResultExperiment>> resultsByTreatment = enzymeResults.stream()
                    .collect(Collectors.groupingBy(ResultExperiment::getTreatment));

            List<TotalResultProcessDTO> processesDTOList = new ArrayList<>();

            for (Map.Entry<Treatment, List<ResultExperiment>> treatmentEntry : resultsByTreatment.entrySet()) {
                Treatment treatment = treatmentEntry.getKey();
                List<ResultExperiment> treatmentResults = treatmentEntry.getValue();

                ProcessInfoDTO processInfo = new ProcessInfoDTO(
                        treatment.getId(), treatment.getName(), treatment.getDescription()
                );
                
                List<TotalResultDataDTO> dataDTOList = new ArrayList<>();
                int repetitionCounter = 1;
                
                for (ResultExperiment r : treatmentResults) {
                    dataDTOList.add(new TotalResultDataDTO(
                            r.getId(),
                            repetitionCounter++,
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