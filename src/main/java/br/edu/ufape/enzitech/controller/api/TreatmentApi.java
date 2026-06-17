package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.model.Treatment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/treatments")
@Tag(name = "Treatments", description = "Endpoints para gestão dos tratamentos dos experimentos")
public interface TreatmentApi {

    @Operation(summary = "Listar Tratamentos", description = "Devolve todos os tratamentos associados a um dado experimento.")
    @GetMapping("/experiment/{experimentId}")
    ResponseEntity<List<Treatment>> getTreatmentsByExperiment(@PathVariable UUID experimentId);

    //passar o DTO
    @Operation(summary = "Criar Tratamento", description = "Regista um novo tratamento na base de dados.")
    @PostMapping
    ResponseEntity<Treatment> createTreatment(@RequestBody Treatment treatment);

    @Operation(summary = "Eliminar Tratamento", description = "Faz a eliminação lógica do tratamento caso não existam resultados.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTreatment(@PathVariable UUID id);
}