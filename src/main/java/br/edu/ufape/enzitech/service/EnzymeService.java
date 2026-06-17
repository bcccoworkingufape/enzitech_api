package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.EnzymeRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.repository.EnzymeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnzymeService {

    private final EnzymeRepository enzymeRepository;

    public Page<Enzyme> findAll(Pageable pageable) {
        return enzymeRepository.findAll(pageable);
    }

    public Enzyme findById(UUID id) {
        return enzymeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enzima não encontrada."));
    }

    @Transactional
    public Enzyme create(EnzymeRequestDTO dto) {
        Enzyme enzyme = new Enzyme();
        enzyme.setName(dto.name());
        enzyme.setType(dto.type());
        enzyme.setDescription(dto.description());
        enzyme.setFormulaCurve(dto.formulaCurve());
        enzyme.setFormulaCalculation(dto.formulaCalculation());
        enzyme.setCreatedAt(LocalDateTime.now());
        enzyme.setUpdatedAt(LocalDateTime.now());
        return enzymeRepository.save(enzyme);
    }

    @Transactional
    public Enzyme update(UUID id, EnzymeRequestDTO dto) {
        Enzyme enzyme = findById(id);
        enzyme.setName(dto.name());
        enzyme.setType(dto.type());
        enzyme.setDescription(dto.description());
        enzyme.setFormulaCurve(dto.formulaCurve());
        enzyme.setFormulaCalculation(dto.formulaCalculation());
        enzyme.setCreatedAt(LocalDateTime.now());
        enzyme.setUpdatedAt(LocalDateTime.now());
        return enzymeRepository.save(enzyme);
    }

    @Transactional
    public void delete(UUID id) {
        Enzyme enzyme = findById(id);
        enzymeRepository.delete(enzyme);
    }
}