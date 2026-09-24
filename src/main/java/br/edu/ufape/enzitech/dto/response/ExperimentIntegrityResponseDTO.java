package br.edu.ufape.enzitech.dto.response;

import java.time.LocalDateTime;

import br.edu.ufape.enzitech.service.ResultSignatureService.IntegrityCheckResult;

public record ExperimentIntegrityResponseDTO(
        boolean signed,
        boolean valid,
        String resultsHash,
        LocalDateTime signedAt
) {
    public static ExperimentIntegrityResponseDTO fromResult(IntegrityCheckResult result) {
        return new ExperimentIntegrityResponseDTO(
                result.signed(),
                result.valid(),
                result.resultsHash(),
                result.signedAt()
        );
    }
}
