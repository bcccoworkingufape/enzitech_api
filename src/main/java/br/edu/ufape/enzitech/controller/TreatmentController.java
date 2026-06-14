package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/treatments")
@RequiredArgsConstructor
@Tag(name = "Treatments", description = "Endpoints para gestão dos tratamentos dos experimentos")
public class TreatmentController {

    private final TreatmentService treatmentService;

    @Operation(summary = "Listar Tratamentos", description = "Devolve todos os tratamentos associados a um dado experimento.")
    @GetMapping("/experiment/{experimentId}")
    public ResponseEntity<List<Treatment>> getTreatmentsByExperiment(@PathVariable UUID experimentId) {
        List<Treatment> treatments = treatmentService.findByExperiment(experimentId);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "Criar Tratamento", description = "Regista um novo tratamento na base de dados.")
    @PostMapping
    public ResponseEntity<Treatment> createTreatment(@RequestBody Treatment treatment) {
        Treatment savedTreatment = treatmentService.save(treatment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTreatment);
    }

    @Operation(summary = "Eliminar Tratamento", description = "Faz a eliminação lógica do tratamento caso não existam resultados associados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTreatment(@PathVariable UUID id) {
        treatmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}