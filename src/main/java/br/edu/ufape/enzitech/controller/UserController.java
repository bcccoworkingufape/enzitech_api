package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.UserApi;
import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable).map(UserResponseDTO::fromEntity));
    }

    @Override
    public ResponseEntity<UserResponseDTO> getUserById(UUID id) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.findById(id)));
    }

    @Override
    public ResponseEntity<UserResponseDTO> createUser(UserRequestDTO dto) {
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDTO.fromEntity(created));
    }

    @Override
    public ResponseEntity<UserResponseDTO> updateUser(UUID id, UserRequestDTO dto) {
        User updated = userService.update(id, dto);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}