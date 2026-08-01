package br.edu.ufape.enzitech.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.enzitech.controller.api.ExperimentApi;
import br.edu.ufape.enzitech.dto.request.ExperimentRequestDTO;
import br.edu.ufape.enzitech.dto.request.SaveRepetitionRequestDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentPaginationResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResponseDTO;
import br.edu.ufape.enzitech.dto.response.ExperimentResultWrapperDTO;
import br.edu.ufape.enzitech.dto.response.RepetitionResponseDTO;
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
    public ResponseEntity<ExperimentPaginationResponseDTO> getMyExperiments(CustomUserDetails userDetails, Boolean finished) {
        UUID loggedUserId = userDetails.getUser().getId();

        List<ExperimentResponseDTO> list = experimentService.findAllByUserId(loggedUserId, finished)
                .stream()
                .map(ExperimentResponseDTO::fromEntity)
                .toList();

        ExperimentPaginationResponseDTO pagination = new ExperimentPaginationResponseDTO(list.size(), list);

        return ResponseEntity.ok(pagination);
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
    public ResponseEntity<List<RepetitionResponseDTO>> getRepetitions(UUID id) {
        return ResponseEntity.ok(calculateExperimentService.getRepetitions(id));
    }

    @Override
    public ResponseEntity<RepetitionResponseDTO> previewRepetition(UUID id, SaveRepetitionRequestDTO dto) {
        return ResponseEntity.ok(calculateExperimentService.previewRepetition(id, dto));
    }

    @Override
    public ResponseEntity<ExperimentResponseDTO> saveRepetition(UUID id, SaveRepetitionRequestDTO dto) {
        return ResponseEntity.ok(calculateExperimentService.saveRepetition(id, dto));
    }

    @Override
    public ResponseEntity<Map<String, List<EnzymeResponseDTO>>> getEnzymesByExperiment(UUID id, Object body) {
        
        List<EnzymeResponseDTO> enzymes = experimentService.getEnzymesByExperiment(id)
                .stream()
                .map(EnzymeResponseDTO::fromExperimentEnzyme)
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