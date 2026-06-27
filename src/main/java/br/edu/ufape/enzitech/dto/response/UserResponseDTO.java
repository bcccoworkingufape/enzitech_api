package br.edu.ufape.enzitech.dto.response;

import java.util.UUID;

import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.enums.Role;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        Role role
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}