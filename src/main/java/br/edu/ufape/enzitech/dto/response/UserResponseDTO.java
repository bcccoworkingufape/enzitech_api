package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

import br.edu.ufape.enzitech.model.User;

public record UserResponseDTO(
        UUID id,
        String name,
        String email
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}