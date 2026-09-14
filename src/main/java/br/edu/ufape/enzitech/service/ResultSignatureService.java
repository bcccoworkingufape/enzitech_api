package br.edu.ufape.enzitech.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.model.RepetitionStatus;
import br.edu.ufape.enzitech.model.ResultExperiment;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.ResultExperimentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultSignatureService {

    private final ResultExperimentRepository resultExperimentRepository;
    private final ExperimentRepository experimentRepository;

    /**
     * Assina o experimento (hash SHA-256 dos resultados) na primeira vez em que ele fica 100%
     * concluído. Não sobrescreve uma assinatura já existente — qualquer resultado alterado depois
     * da assinatura passa a divergir do hash original, o que é detectado em {@link #verifyIntegrity}.
     */
    public void signIfComplete(Experiment experiment) {
        if (experiment.getResultsHash() != null) return;
        if (experiment.getProgress() == null || experiment.getProgress() < 1.0) return;

        experiment.setResultsHash(computeHash(experiment.getId()));
        experiment.setResultsSignedAt(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public IntegrityCheckResult verifyIntegrity(UUID experimentId) {
        Experiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new EntityNotFoundException("Experimento não encontrado."));

        if (experiment.getResultsHash() == null) {
            return new IntegrityCheckResult(false, false, null, null);
        }

        String currentHash = computeHash(experimentId);
        boolean valid = currentHash.equals(experiment.getResultsHash());
        return new IntegrityCheckResult(true, valid, experiment.getResultsHash(), experiment.getResultsSignedAt());
    }

    private String computeHash(UUID experimentId) {
        List<ResultExperiment> results = resultExperimentRepository.findByExperimentId(experimentId).stream()
                .filter(r -> r.getStatus() == RepetitionStatus.COMPLETED)
                .sorted(Comparator.<ResultExperiment, UUID>comparing(r -> r.getTreatment().getId())
                        .thenComparing(r -> r.getEnzyme().getId())
                        .thenComparing(ResultExperiment::getRepetitionNumber))
                .toList();

        StringBuilder payload = new StringBuilder("experiment:").append(experimentId).append('|');
        for (ResultExperiment r : results) {
            payload.append(r.getTreatment().getId()).append(',')
                    .append(r.getEnzyme().getId()).append(',')
                    .append(r.getRepetitionNumber()).append(',')
                    .append(r.getSample()).append(',')
                    .append(r.getWhiteSample()).append(',')
                    .append(r.getDifferenceBetweenSamples()).append(',')
                    .append(r.getCurve()).append(',')
                    .append(r.getResult()).append(',')
                    .append(r.getStatus())
                    .append(';');
        }

        return sha256Hex(payload.toString());
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", e);
        }
    }

    public record IntegrityCheckResult(boolean signed, boolean valid, String resultsHash, LocalDateTime signedAt) {}
}
