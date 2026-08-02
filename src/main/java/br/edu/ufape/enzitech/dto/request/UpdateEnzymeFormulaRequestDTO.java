package br.edu.ufape.enzitech.dto.request;

public record UpdateEnzymeFormulaRequestDTO(
        String customFormulaCurve,
        String customFormulaCalculation
) {}
