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
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.SaveRepetitionRequestDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentPaginationResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResultWrapperDTO;
import br.edu.ufape.enzitech.dto.response.RepetitionResponseDTO;
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
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Filtra por experimentos concluídos (progress = 100%) ou em andamento.")
            @RequestParam(required = false) Boolean finished
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

    @Operation(summary = "Atualizar Experimento", description = "Atualiza nome, descrição, repetições e a seleção de tratamentos/enzimas de um experimento. Tratamentos/enzimas removidos que já possuem repetições concluídas são preservados como histórico inativo, nunca apagados.")
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

    @Operation(summary = "Listar Repetições", description = "Lista todos os slots de repetição (pendentes e concluídos) do experimento.")
    @GetMapping("/{id}/repetitions")
    ResponseEntity<List<RepetitionResponseDTO>> getRepetitions(@PathVariable UUID id);

    @Operation(summary = "Pré-visualizar Repetição", description = "Calcula o resultado de uma repetição sem persistir.")
    @PostMapping("/{id}/repetitions/preview")
    ResponseEntity<RepetitionResponseDTO> previewRepetition(
            @PathVariable UUID id,
            @RequestBody @Valid SaveRepetitionRequestDTO dto
    );

    @Operation(summary = "Salvar Repetição", description = "Calcula e salva uma única repetição, de forma parcial/isolada — não exige que as demais repetições estejam preenchidas.")
    @PutMapping("/{id}/repetitions")
    ResponseEntity<ExperimentResponseDTO> saveRepetition(
            @PathVariable UUID id,
            @RequestBody @Valid SaveRepetitionRequestDTO dto
    );

    @Operation(summary = "Obter Resultado Total do Experimento")
    @GetMapping("/get-total-result/{id}")
    ResponseEntity<ExperimentResultWrapperDTO> getTotalResult(@PathVariable UUID id);
}