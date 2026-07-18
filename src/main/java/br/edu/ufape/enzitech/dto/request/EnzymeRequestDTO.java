package br.edu.ufape.enzitech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnzymeRequestDTO(
        @NotBlank(message = "O nome da enzima é obrigatório.")
        String name,

        @NotNull(message = "A variável A é obrigatória.")
        Double variableA,

        @NotNull(message = "A variável B é obrigatória.")
        Double variableB,

        @NotBlank(message = "O tipo da enzima é obrigatório.")
        String type
) {}