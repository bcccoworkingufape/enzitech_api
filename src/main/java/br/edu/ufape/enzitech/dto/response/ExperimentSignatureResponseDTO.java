package br.edu.ufape.enzitech.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.ufape.enzitech.model.ExperimentSignature;

public record ExperimentSignatureResponseDTO(
        UUID id,
        String resultsHash,
        LocalDateTime signedAt
) {
    public static ExperimentSignatureResponseDTO fromEntity(ExperimentSignature signature) {
        return new ExperimentSignatureResponseDTO(
                signature.getId(),
                signature.getResultsHash(),
                signature.getSignedAt()
        );
    }
}
