package br.edu.ufape.enzitech.dto.response;

public record AuthResponseDTO(
        String token,
        UserResponseDTO user
) {}