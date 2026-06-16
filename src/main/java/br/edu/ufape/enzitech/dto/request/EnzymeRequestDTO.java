package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EnzymeRequestDTO(
        @NotBlank(message = "O nome da enzima é obrigatório.")
        String name,
        
        @NotBlank(message = "O tipo da enzima é obrigatório.")
        String type,
        
        String description,
        
        @NotBlank(message = "A fórmula da curva é obrigatória.")
        String formulaCurve,
        
        @NotBlank(message = "A fórmula de cálculo final é obrigatória.")
        String formulaCalculation
) {}