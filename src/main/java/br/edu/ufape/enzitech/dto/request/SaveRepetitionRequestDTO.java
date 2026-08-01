package br.edu.ufape.enzitech.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveRepetitionRequestDTO(

        @NotNull(message = "O ID do tratamento é obrigatório.")
        UUID treatmentId,

        @NotNull(message = "O ID da enzima é obrigatório.")
        UUID enzymeId,

        @NotNull(message = "O número da repetição é obrigatório.")
        @Min(value = 1, message = "O número da repetição deve ser maior ou igual a 1.")
        Integer repetitionNumber,

        @NotNull(message = "O valor da amostra não pode ser nulo.")
        Double sample,

        @NotNull(message = "O valor da amostra em branco não pode ser nulo.")
        Double whiteSample
) {}
