package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.CalculateExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentDataRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import lombok.RequiredArgsConstructor;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalculateExperimentService {

    private final ExperimentEnzymeRepository configRepository;
    private final ResultExperimentRepository resultRepository;
    private final TreatmentService treatmentService;

    @Transactional
    public List<ResultExperiment> calculateAndSaveResults(UUID experimentId, CalculateExperimentRequestDTO dto) {
        ExperimentEnzyme config = configRepository.findByExperimentIdAndEnzymeId(experimentId, dto.enzymeId())
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

            savedResults.add(result);
        }

        return resultRepository.saveAll(savedResults);
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
}