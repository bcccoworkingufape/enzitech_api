package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

public record ProcessInfoDTO(
        UUID id,
        String name,
        String description
) {}