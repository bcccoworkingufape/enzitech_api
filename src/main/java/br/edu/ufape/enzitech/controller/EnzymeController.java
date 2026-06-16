package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.EnzymeApi;
import br.edu.ufape.enzitech.dto.request.EnzymeRequestDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.service.EnzymeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EnzymeController implements EnzymeApi {

    private final EnzymeService enzymeService;

    @Override
    public ResponseEntity<Page<EnzymeResponseDTO>> getAllEnzymes(Pageable pageable) {
        Page<EnzymeResponseDTO> enzymes = enzymeService.findAll(pageable)
                .map(EnzymeResponseDTO::fromEntity);
        return ResponseEntity.ok(enzymes);
    }

    @Override
    public ResponseEntity<EnzymeResponseDTO> getEnzymeById(UUID id) {
        return ResponseEntity.ok(EnzymeResponseDTO.fromEntity(enzymeService.findById(id)));
    }

    @Override
    public ResponseEntity<EnzymeResponseDTO> createEnzyme(EnzymeRequestDTO dto) {
        Enzyme created = enzymeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnzymeResponseDTO.fromEntity(created));
    }

    @Override
    public ResponseEntity<EnzymeResponseDTO> updateEnzyme(UUID id, EnzymeRequestDTO dto) {
        Enzyme updated = enzymeService.update(id, dto);
        return ResponseEntity.ok(EnzymeResponseDTO.fromEntity(updated));
    }

    @Override
    public ResponseEntity<Void> deleteEnzyme(UUID id) {
        enzymeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}