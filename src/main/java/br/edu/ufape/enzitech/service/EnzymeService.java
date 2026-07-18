package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.EnzymeRequestDTO;
import br.edu.ufape.enzitech.model.Enzyme;
import br.edu.ufape.enzitech.repository.EnzymeRepository;
import lombok.RequiredArgsConstructor;

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
        enzyme.setVariableA(dto.variableA());
        enzyme.setVariableB(dto.variableB());
        enzyme.setDescription(getFormulaEnzyme(dto.type()));
        enzyme.setFormulaCurve("(difference - variableB) / variableA"); 
        
        if (dto.type().equals("Urease")) {
            enzyme.setFormulaCalculation("(curve * size) / (duration * weightSample * weightGround)");
        } else {
            enzyme.setFormulaCalculation("(curve * size) / (duration * weightSample * weightGround)");
        }

        enzyme.setCreatedAt(LocalDateTime.now());
        enzyme.setUpdatedAt(LocalDateTime.now());
        return enzymeRepository.save(enzyme);
    }

    private String getFormulaEnzyme(String enzymeType) {
        return switch (enzymeType) {
            case "FosfataseAcida", "FosfataseAlcalina" -> "µg PNP g-1 solo h-1";
            case "Betaglucosidase" -> "μg PNG g-1 de solo h-1";
            case "Aryl" -> "µg PNS g-1 solo h-1";
            case "Urease" -> "μg NH4-N g-1 dwt 2h-1";
            case "FDA" -> "µg g-1 solo.seco h-1";
            default -> null;
        };
    }

    @Transactional
    public Enzyme update(UUID id, EnzymeRequestDTO dto) {
        Enzyme enzyme = findById(id);
        enzyme.setName(dto.name());
        enzyme.setType(dto.type());
        enzyme.setVariableA(dto.variableA());
        enzyme.setVariableB(dto.variableB());
        enzyme.setDescription(getFormulaEnzyme(dto.type()));
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