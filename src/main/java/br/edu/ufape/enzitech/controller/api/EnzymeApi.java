package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.EnzymeRequestDTO;
import br.edu.ufape.enzitech.dto.response.EnzymeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/enzymes")
@Tag(name = "Enzymes", description = "Endpoints para gestão das enzimas e suas fórmulas")
public interface EnzymeApi {

    @Operation(summary = "Listar Enzimas", description = "Devolve todas as enzimas de forma paginada.")
    @GetMapping
    ResponseEntity<Page<EnzymeResponseDTO>> getAllEnzymes(Pageable pageable);

    @Operation(summary = "Buscar Enzima", description = "Devolve uma enzima específica pelo seu ID.")
    @GetMapping("/{id}")
    ResponseEntity<EnzymeResponseDTO> getEnzymeById(@PathVariable UUID id);

    @Operation(summary = "Criar Enzima", description = "Regista uma nova enzima com as suas fórmulas matemáticas.")
    @PostMapping
    ResponseEntity<EnzymeResponseDTO> createEnzyme(@RequestBody @Valid EnzymeRequestDTO dto);

    @Operation(summary = "Atualizar Enzima", description = "Atualiza os dados e fórmulas de uma enzima.")
    @PutMapping("/{id}")
    ResponseEntity<EnzymeResponseDTO> updateEnzyme(@PathVariable UUID id, @RequestBody @Valid EnzymeRequestDTO dto);

    @Operation(summary = "Eliminar Enzima", description = "Remove uma enzima da base de dados.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteEnzyme(@PathVariable UUID id);
}