package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.ExperimentApi;
import br.edu.ufape.enzitech.dto.request.CalculateExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.model.Experiment;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.service.CalculateExperimentService;
import br.edu.ufape.enzitech.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ExperimentController implements ExperimentApi {

    private final ExperimentService experimentService;

    private final CalculateExperimentService calculateExperimentService;


    @Override
    public ResponseEntity<Page<ExperimentResponseDTO>> getMyExperiments(CustomUserDetails userDetails, Pageable pageable) {
        UUID loggedUserId = userDetails.getUser().getId();
        
        Page<ExperimentResponseDTO> experiments = experimentService.findAllByUserId(loggedUserId, pageable)
                .map(ExperimentResponseDTO::fromEntity);
        
        return ResponseEntity.ok(experiments);
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
    public ResponseEntity<Void> calculateExperiment(UUID id, CalculateExperimentRequestDTO dto) {
        calculateExperimentService.calculateAndSaveResults(id, dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}