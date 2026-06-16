package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.TreatmentApi;
import br.edu.ufape.enzitech.model.Treatment;
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
    public ResponseEntity<List<Treatment>> getTreatmentsByExperiment(UUID experimentId) {
        List<Treatment> treatments = treatmentService.findByExperiment(experimentId);
        return ResponseEntity.ok(treatments);
    }

    @Override
    public ResponseEntity<Treatment> createTreatment(Treatment treatment) {
        Treatment savedTreatment = treatmentService.save(treatment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTreatment);
    }

    @Override
    public ResponseEntity<Void> deleteTreatment(UUID id) {
        treatmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}