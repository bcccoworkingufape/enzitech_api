package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.UserApi;
import br.edu.ufape.enzitech.dto.request.PromoteAdminRequestDTO;
import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.UserAvatar;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable).map(UserResponseDTO::fromEntity));
    }

    @Override
    public ResponseEntity<UserResponseDTO> getMe(CustomUserDetails userDetails) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.findById(userDetails.getUser().getId())));
    }

    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<UserResponseDTO> updateUser(UUID id, UserRequestDTO dto, CustomUserDetails userDetails) {
        User updated = userService.update(id, dto, userDetails.getUser());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserResponseDTO> promoteToAdmin(PromoteAdminRequestDTO dto, CustomUserDetails userDetails) {
        User promoted = userService.promoteToAdmin(dto.email(), userDetails.getUser());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(promoted));
    }

    @Override
    public ResponseEntity<Void> deleteMyAccount(CustomUserDetails userDetails) {
        userService.deleteOwnAccount(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<byte[]> getAvatar(UUID id, String v, CustomUserDetails userDetails) {
        UserAvatar avatar = userService.findAvatar(id, userDetails.getUser());
        // A URL versionada (?v=avatarUpdatedAt) muda a cada troca de foto, então o navegador
        // pode guardá-la para sempre e nem chegar a consultar a API nas próximas telas.
        CacheControl cache = v != null
                ? CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable()
                : CacheControl.noCache().cachePrivate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .cacheControl(cache)
                .body(avatar.getContent());
    }

    @Override
    public ResponseEntity<UserResponseDTO> updateMyAvatar(MultipartFile file, CustomUserDetails userDetails) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.updateAvatar(userDetails.getUser(), file)));
    }

    @Override
    public ResponseEntity<UserResponseDTO> deleteMyAvatar(CustomUserDetails userDetails) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.deleteAvatar(userDetails.getUser())));
    }
}
