package br.edu.ufape.enzitech.controller.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.edu.ufape.enzitech.dto.request.CalculateExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.response.CalculateExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentPaginationResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResultWrapperDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultExperimentDTO;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping("/experiments")
@Tag(name = "Experiments", description = "Endpoints para gestão de experimentos")
public interface ExperimentApi {

    @Operation(summary = "Listar Experimentos", description = "Devolve os experimentos pertencentes ao utilizador autenticado.")
    @GetMapping
    ResponseEntity<ExperimentPaginationResponseDTO> getMyExperiments(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
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

    @Operation(summary = "Listar Enzimas do Experimento", description = "Busca as enzimas associadas a um experimento (via POST para manter compatibilidade com o legado).")
    @PostMapping("/get-enzymes/{id}")
    ResponseEntity<Map<String, List<EnzymeResponseDTO>>> getEnzymesByExperiment(
            @PathVariable UUID id,
            @RequestBody(required = false) Object body
    );

    @Operation(summary = "Calcular Resultados (Prévia)")
    @PostMapping("/calculate/{id}") 
    ResponseEntity<CalculateExperimentResponseDTO> calculateExperiment(
            @PathVariable UUID id,
            @RequestBody @Valid CalculateExperimentRequestDTO dto
    );

    @Operation(summary = "Salvar Resultados Calculados")
    @PostMapping("/save-result/{id}") 
    ResponseEntity<ExperimentResponseDTO> saveResultExperiment(
            @PathVariable UUID id,
            @RequestBody @Valid CalculateExperimentRequestDTO dto
    );

    @Operation(summary = "Obter Resultado Total do Experimento")
    @GetMapping("/get-total-result/{id}")
    ResponseEntity<ExperimentResultWrapperDTO> getTotalResult(@PathVariable UUID id);
}