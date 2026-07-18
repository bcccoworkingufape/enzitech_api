package br.edu.ufape.enzitech.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.enzitech.controller.api.ExperimentApi;
import br.edu.ufape.enzitech.dto.request.CalculateExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.response.CalculateExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentPaginationResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResultWrapperDTO;
import br.edu.ufape.enzitech.dto.response.TotalResultExperimentDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.service.CalculateExperimentService;
import br.edu.ufape.enzitech.service.ExperimentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExperimentController implements ExperimentApi {

    private final ExperimentService experimentService;

    private final CalculateExperimentService calculateExperimentService;


    @Override
    public ResponseEntity<ExperimentPaginationResponseDTO> getMyExperiments(CustomUserDetails userDetails) {
        UUID loggedUserId = userDetails.getUser().getId();

        List<ExperimentResponseDTO> list = experimentService.findAllByUserId(loggedUserId)
                .stream()
                .map(ExperimentResponseDTO::fromEntity)
                .toList();
        
        ExperimentPaginationResponseDTO pagination = new ExperimentPaginationResponseDTO(list.size(), list);
        
        return ResponseEntity.ok(pagination);
    }

    @Override
    public ResponseEntity<ExperimentResponseDTO> saveResultExperiment(UUID id, CalculateExperimentRequestDTO dto) {
        calculateExperimentService.saveResults(id, dto);

        Experiment updatedExperiment = experimentService.findById(id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ExperimentResponseDTO.fromEntity(updatedExperiment));
    }

    @Override
    public ResponseEntity<ExperimentResponseDTO> getExperimentById(UUID id) {
        return ResponseEntity.ok(ExperimentResponseDTO.fromEntity(experimentService.findById(id)));
    }

    @Override
    public ResponseEntity<ExperimentResponseDTO> createExperiment(ExperimentRequestDTO dto, CustomUserDetails userDetails) {
        Experiment created = experimentService.create(dto, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExperimentResponseDTO.fromEntity(created));
    }

    @Override
    public ResponseEntity<ExperimentResponseDTO> updateExperiment(UUID id, ExperimentRequestDTO dto) {
        Experiment updated = experimentService.update(id, dto);
        return ResponseEntity.ok(ExperimentResponseDTO.fromEntity(updated));
    }

    @Override
    public ResponseEntity<Void> deleteExperiment(UUID id) {
        experimentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CalculateExperimentResponseDTO> calculateExperiment(UUID id, CalculateExperimentRequestDTO dto) {
        
        CalculateExperimentResponseDTO preview = calculateExperimentService.calculatePreview(id, dto);

        System.out.println("=== PREVIA ENVIADA PARA O FLUTTER ===");
        System.out.println("Resultados (Repetições): " + preview.results());
        System.out.println("Média Geral: " + preview.average());

        return ResponseEntity.status(HttpStatus.CREATED).body(preview);
    }

    @Override
    public ResponseEntity<Map<String, List<EnzymeResponseDTO>>> getEnzymesByExperiment(UUID id, Object body) {
        
        List<EnzymeResponseDTO> enzymes = experimentService.getEnzymesByExperiment(id)
                .stream()
                .map(EnzymeResponseDTO::fromEntity)
                .toList();

        Map<String, List<EnzymeResponseDTO>> response = Map.of("enzymes", enzymes);
        
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ExperimentResultWrapperDTO> getTotalResult(UUID id) {
        List<TotalResultExperimentDTO> totalResultList = calculateExperimentService.getTotalResult(id);

        ExperimentResultWrapperDTO wrapper = new ExperimentResultWrapperDTO(totalResultList);

        return ResponseEntity.ok(wrapper);
    }
}