package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.TreatmentApi;
import br.edu.ufape.enzitech.dto.request.TreatmentRequestDTO;
import br.edu.ufape.enzitech.dto.response.TreatmentResponseDTO;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TreatmentController implements TreatmentApi {

    private final TreatmentService treatmentService;

    @Override
    public ResponseEntity<List<TreatmentResponseDTO>> getTreatmentsByExperiment(UUID experimentId) {
        List<TreatmentResponseDTO> treatments = treatmentService.findByExperiment(experimentId)
                .stream()
                .map(TreatmentResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(treatments);
    }

    @Override
    public ResponseEntity<List<TreatmentResponseDTO>> getTreatmentsByLoggedUser(CustomUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();
        List<TreatmentResponseDTO> treatments = treatmentService.findByUser(userId)
                .stream()
                .map(TreatmentResponseDTO::fromEntity)
                .toList();
                
        return ResponseEntity.ok(treatments);
    }

    @Override
    public ResponseEntity<TreatmentResponseDTO> createTreatment(TreatmentRequestDTO dto, CustomUserDetails userDetails) {
        Treatment savedTreatment = treatmentService.create(dto, userDetails.getUser());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TreatmentResponseDTO.fromEntity(savedTreatment));
    }

    @Override
    public ResponseEntity<Void> deleteTreatment(UUID id) {
        treatmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}