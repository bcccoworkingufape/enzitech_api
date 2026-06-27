package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.TreatmentRequestDTO;
import br.edu.ufape.enzitech.dto.response.TreatmentResponseDTO;
import br.edu.ufape.enzitech.model.Treatment;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/treatments")
@Tag(name = "Treatments", description = "Endpoints para gestão dos tratamentos dos experimentos")
public interface TreatmentApi {

    @Operation(summary = "Listar Tratamentos", description = "Devolve todos os tratamentos associados a um dado experimento.")
    @GetMapping("/experiment/{experimentId}")
    ResponseEntity<List<Treatment>> getTreatmentsByExperiment(@PathVariable UUID experimentId);

    @Operation(summary = "Criar Tratamento", description = "Regista um novo tratamento na base de dados.")
    @PostMapping
    ResponseEntity<TreatmentResponseDTO> createTreatment(
            @RequestBody @Valid TreatmentRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Listar Tratamentos do usuário logado", description = "Devolve todos os tratamentos associados ao usuário logado.")
    @GetMapping("/user")
    ResponseEntity<List<Treatment>> getTreatmentsByLoggedUser(Authentication authentication);


    @Operation(summary = "Eliminar Tratamento", description = "Faz a eliminação lógica do tratamento caso não existam resultados.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTreatment(@PathVariable UUID id);
}