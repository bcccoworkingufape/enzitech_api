package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/experiments")
@Tag(name = "Experiments", description = "Endpoints para gestão de experimentos")
public interface ExperimentApi {

    @Operation(summary = "Listar Experimentos", description = "Devolve os experimentos pertencentes ao utilizador autenticado.")
    @GetMapping
    ResponseEntity<Page<ExperimentResponseDTO>> getMyExperiments(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    );

    @Operation(summary = "Buscar Experimento", description = "Busca um experimento pelo ID.")
    @GetMapping("/{id}")
    ResponseEntity<ExperimentResponseDTO> getExperimentById(@PathVariable UUID id);

    @Operation(summary = "Criar Experimento", description = "Cria um novo experimento associado ao utilizador logado.")
    @PostMapping
    ResponseEntity<ExperimentResponseDTO> createExperiment(
            @RequestBody @Valid ExperimentRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Atualizar Experimento", description = "Atualiza nome e descrição de um experimento.")
    @PutMapping("/{id}")
    ResponseEntity<ExperimentResponseDTO> updateExperiment(
            @PathVariable UUID id, 
            @RequestBody @Valid ExperimentRequestDTO dto
    );

    @Operation(summary = "Deletar Experimento", description = "Faz o soft delete do experimento.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteExperiment(@PathVariable UUID id);
}