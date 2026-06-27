package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.TreatmentApi;
import br.edu.ufape.enzitech.dto.request.TreatmentRequestDTO;
import br.edu.ufape.enzitech.dto.response.TreatmentResponseDTO;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.service.TreatmentService;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TreatmentController implements TreatmentApi {

    private final TreatmentService treatmentService;

    @Override
    public ResponseEntity<List<Treatment>> getTreatmentsByExperiment(UUID experimentId) {
        List<Treatment> treatments = treatmentService.findByExperiment(experimentId);
        return ResponseEntity.ok(treatments);
    }

    @Override
    public ResponseEntity<List<Treatment>> getTreatmentsByLoggedUser(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUser().getId();

        List<Treatment> treatments = treatmentService.findByUser(userId);
        return ResponseEntity.ok(treatments);
    }

    @Override
    public ResponseEntity<TreatmentResponseDTO> createTreatment(TreatmentRequestDTO dto, CustomUserDetails userDetails) {
        Treatment savedTreatment = treatmentService.create(dto, userDetails.getUser());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(TreatmentResponseDTO.fromEntity(savedTreatment));
    }

    @Override
    public ResponseEntity<Void> deleteTreatment(UUID id) {
        treatmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}