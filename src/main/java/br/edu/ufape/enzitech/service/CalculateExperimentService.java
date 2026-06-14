package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.model.*;
import br.edu.ufape.enzitech.repository.ExperimentEnzymeRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalculateExperimentService {

    private final ExperimentEnzymeRepository configRepository;
    private final ResultExperimentRepository resultRepository;
    private final TreatmentService treatmentService;

    @Transactional
    public ResultExperiment calculateAndSaveResult(UUID experimentId, UUID enzymeId, UUID treatmentId, Double sample, Double whiteSample) {
        ExperimentEnzyme config = configRepository.findByExperimentIdAndEnzymeId(experimentId, enzymeId)
                .orElseThrow(() -> new RuntimeException("Configuração de Enzima não encontrada para este Experimento."));

        Treatment treatment = treatmentService.findById(treatmentId);

        Double difference = sample - whiteSample;
        Double curve = (difference * config.getVariableA()) + config.getVariableB(); 
        Double finalResult = (curve * config.getSize()) / (config.getDuration() * config.getWeightSample());

        ResultExperiment result = new ResultExperiment();
        result.setExperiment(config.getExperiment());
        result.setEnzyme(config.getEnzyme());
        result.setTreatment(treatment);
        result.setSample(sample);
        result.setWhiteSample(whiteSample);
        result.setDifferenceBetweenSamples(difference);
        result.setCurve(curve);
        result.setResult(finalResult);

        return resultRepository.save(result);
    }
}