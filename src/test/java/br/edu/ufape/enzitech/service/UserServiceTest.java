package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.UserAvatar;
import br.edu.ufape.enzitech.model.enums.Role;
import br.edu.ufape.enzitech.repository.ExperimentRepository;
import br.edu.ufape.enzitech.repository.TreatmentRepository;
import br.edu.ufape.enzitech.repository.UserAvatarRepository;
import br.edu.ufape.enzitech.repository.UserRepository;
import br.edu.ufape.enzitech.security.RateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ExperimentRepository experimentRepository;
    @Mock private TreatmentRepository treatmentRepository;
    @Mock private RateLimiter rateLimiter;
    @Mock private UserAvatarRepository userAvatarRepository;

    @InjectMocks
    private UserService userService;

    private final UserRequestDTO dto = new UserRequestDTO("Novo Nome", "novo@ufape.edu.br", "Nova@Senha1");

    @Test
    void commonUserCannotEditAnotherAccount() {
        User requester = user(Role.USER);
        UUID victimId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.update(victimId, dto, requester))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(userRepository, never()).save(any());
    }

    @Test
    void userCanEditOwnAccountAndPasswordChangeRevokesOldTokens() {
        User requester = user(Role.USER);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("hash");
        when(userRepository.save(requester)).thenReturn(requester);

        userService.update(requester.getId(), dto, requester);

        assertThat(requester.getCredentialsUpdatedAt()).isNotNull();
        assertThat(requester.getPassword()).isEqualTo("hash");
    }

    @Test
    void adminCanEditAnotherAccount() {
        User admin = user(Role.ADMIN);
        User target = user(Role.USER);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("hash");
        when(userRepository.save(target)).thenReturn(target);

        assertThat(userService.update(target.getId(), dto, admin)).isSameAs(target);
    }

    @Test
    void commonUserCannotReadAnotherUsersAvatar() {
        User requester = user(Role.USER);

        assertThatThrownBy(() -> userService.findAvatar(UUID.randomUUID(), requester))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(userAvatarRepository, never()).findById(any());
    }

    @Test
    void adminCanReadAnotherUsersAvatar() {
        User admin = user(Role.ADMIN);
        UUID targetId = UUID.randomUUID();
        UserAvatar avatar = new UserAvatar();
        when(userAvatarRepository.findById(targetId)).thenReturn(Optional.of(avatar));

        assertThat(userService.findAvatar(targetId, admin)).isSameAs(avatar);
    }

    @Test
    void uploadingAvatarStoresDetectedTypeAndBumpsVersion() {
        User requester = user(Role.USER);
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(userAvatarRepository.findById(requester.getId())).thenReturn(Optional.empty());
        when(userRepository.save(requester)).thenReturn(requester);
        // Content-Type declarado pelo cliente é ignorado; vale o que os bytes dizem.
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", AvatarImageTest.png(256, 256));

        userService.updateAvatar(requester, file);

        assertThat(requester.getAvatarUpdatedAt()).isNotNull();
        verify(userAvatarRepository).save(argThat(a ->
                a.getUserId().equals(requester.getId()) && a.getContentType().equals("image/png")));
    }

    @Test
    void invalidAvatarIsRejectedWithoutTouchingTheDatabase() {
        User requester = user(Role.USER);
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "<svg/>".getBytes());

        assertThatThrownBy(() -> userService.updateAvatar(requester, file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(userAvatarRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deletingOwnAccountAlsoErasesAvatar() {
        User requester = user(Role.USER);
        requester.setAvatarUpdatedAt(java.time.LocalDateTime.now());

        userService.deleteOwnAccount(requester);

        verify(userAvatarRepository).deleteById(requester.getId());
        assertThat(requester.getAvatarUpdatedAt()).isNull();
    }

    private static User user(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(role);
        user.setEmail(UUID.randomUUID() + "@ufape.edu.br");
        return user;
    }
}
