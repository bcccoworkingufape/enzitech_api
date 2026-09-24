package br.edu.ufape.enzitech.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.ExperimentEnzyme;
import br.edu.ufape.enzitech.model.ExperimentSignature;
import br.edu.ufape.enzitech.model.ExperimentTreatment;
import br.edu.ufape.enzitech.model.RepetitionStatus;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ExperimentSignatureRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import br.edu.ufape.enzitech.service.ResultSignatureService.IntegrityCheckResult;

@ExtendWith(MockitoExtension.class)
class ResultSignatureServiceTest {

    @Mock
    private ResultExperimentRepository resultExperimentRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentSignatureRepository experimentSignatureRepository;

    private ResultSignatureService resultSignatureService;

    private UUID experimentId;
    private ExperimentTreatment treatment;
    private ExperimentEnzyme enzyme;

    @BeforeEach
    void setUp() {
        resultSignatureService = new ResultSignatureService(
                resultExperimentRepository, experimentRepository, experimentSignatureRepository);

        experimentId = UUID.randomUUID();

        treatment = new ExperimentTreatment();
        treatment.setId(UUID.randomUUID());

        enzyme = new ExperimentEnzyme();
        enzyme.setId(UUID.randomUUID());
    }

    private ResultExperiment completedResult(int repetitionNumber, double result) {
        ResultExperiment r = new ResultExperiment();
        r.setId(UUID.randomUUID());
        r.setTreatment(treatment);
        r.setEnzyme(enzyme);
        r.setRepetitionNumber(repetitionNumber);
        r.setStatus(RepetitionStatus.COMPLETED);
        r.setSample(10.0);
        r.setWhiteSample(1.0);
        r.setDifferenceBetweenSamples(9.0);
        r.setCurve(2.0);
        r.setResult(result);
        return r;
    }

    @Test
    void reconcileSignature_doesNothing_whenProgressNotFull() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(0.5);

        resultSignatureService.reconcileSignature(experiment);

        assertThat(experiment.getResultsHash()).isNull();
        assertThat(experiment.getResultsSignedAt()).isNull();
    }

    @Test
    void reconcileSignature_generatesHash_whenProgressReaches100Percent() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(1.0);

        when(resultExperimentRepository.findByExperimentId(experimentId))
                .thenReturn(List.of(completedResult(1, 42.0)));

        resultSignatureService.reconcileSignature(experiment);

        assertThat(experiment.getResultsHash()).isNotBlank().hasSize(64);
        assertThat(experiment.getResultsSignedAt()).isNotNull();
        verify(experimentSignatureRepository, times(1)).save(any(ExperimentSignature.class));
    }

    @Test
    void reconcileSignature_doesNotOverwrite_existingSignature() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(1.0);
        experiment.setResultsHash("already-signed");

        resultSignatureService.reconcileSignature(experiment);

        assertThat(experiment.getResultsHash()).isEqualTo("already-signed");
        verify(experimentSignatureRepository, times(0)).save(any(ExperimentSignature.class));
    }

    @Test
    void reconcileSignature_clearsCurrentHash_whenExperimentReopens() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(0.8);
        experiment.setResultsHash("hash-from-previous-closing");
        experiment.setResultsSignedAt(java.time.LocalDateTime.now().minusDays(1));

        resultSignatureService.reconcileSignature(experiment);

        assertThat(experiment.getResultsHash()).isNull();
        assertThat(experiment.getResultsSignedAt()).isNull();
        verify(experimentSignatureRepository, times(0)).save(any(ExperimentSignature.class));
    }

    @Test
    void reconcileSignature_generatesNewSignature_whenExperimentClosesAgainAfterReopening() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);

        experiment.setProgress(1.0);
        when(resultExperimentRepository.findByExperimentId(experimentId))
                .thenReturn(List.of(completedResult(1, 42.0)));
        resultSignatureService.reconcileSignature(experiment);
        String firstHash = experiment.getResultsHash();

        experiment.setProgress(0.5);
        resultSignatureService.reconcileSignature(experiment);
        assertThat(experiment.getResultsHash()).isNull();

        experiment.setProgress(1.0);
        when(resultExperimentRepository.findByExperimentId(experimentId))
                .thenReturn(List.of(completedResult(1, 42.0), completedResult(2, 99.0)));
        resultSignatureService.reconcileSignature(experiment);

        assertThat(experiment.getResultsHash()).isNotBlank().isNotEqualTo(firstHash);
        verify(experimentSignatureRepository, times(2)).save(any(ExperimentSignature.class));
    }

    @Test
    void verifyIntegrity_isValid_whenResultsUnchangedSinceSigning() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(1.0);

        List<ResultExperiment> results = List.of(completedResult(1, 42.0));
        when(resultExperimentRepository.findByExperimentId(experimentId)).thenReturn(results);

        resultSignatureService.reconcileSignature(experiment);
        when(experimentRepository.findById(experimentId)).thenReturn(Optional.of(experiment));

        IntegrityCheckResult check = resultSignatureService.verifyIntegrity(experimentId);

        assertThat(check.signed()).isTrue();
        assertThat(check.valid()).isTrue();
        assertThat(check.resultsHash()).isEqualTo(experiment.getResultsHash());
    }

    @Test
    void verifyIntegrity_isInvalid_whenAResultIsAlteredAfterSigning() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(1.0);

        ResultExperiment original = completedResult(1, 42.0);
        when(resultExperimentRepository.findByExperimentId(experimentId)).thenReturn(List.of(original));
        resultSignatureService.reconcileSignature(experiment);

        when(experimentRepository.findById(experimentId)).thenReturn(Optional.of(experiment));

        original.setResult(999.0);

        IntegrityCheckResult check = resultSignatureService.verifyIntegrity(experimentId);

        assertThat(check.signed()).isTrue();
        assertThat(check.valid()).isFalse();
    }

    @Test
    void verifyIntegrity_notSigned_whenExperimentNeverClosed() {
        Experiment experiment = new Experiment();
        experiment.setId(experimentId);
        experiment.setProgress(0.5);

        when(experimentRepository.findById(experimentId)).thenReturn(Optional.of(experiment));

        IntegrityCheckResult check = resultSignatureService.verifyIntegrity(experimentId);

        assertThat(check.signed()).isFalse();
        assertThat(check.valid()).isFalse();
    }

    @Test
    void getHistory_returnsAllPastSignatures_mostRecentFirst() {
        ExperimentSignature older = new ExperimentSignature();
        ExperimentSignature newer = new ExperimentSignature();
        when(experimentSignatureRepository.findByExperimentIdOrderBySignedAtDesc(experimentId))
                .thenReturn(List.of(newer, older));

        List<ExperimentSignature> history = resultSignatureService.getHistory(experimentId);

        assertThat(history).containsExactly(newer, older);
    }
}
