package br.edu.ufape.enzitech.service;

import br.edu.ufape.enzitech.dto.request.ForgotPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.request.ResetPasswordRequestDTO;
import br.edu.ufape.enzitech.model.PasswordResetToken;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.PasswordResetTokenRepository;
import br.edu.ufape.enzitech.repository.UserRepository;
import br.edu.ufape.enzitech.security.JwtService;
import br.edu.ufape.enzitech.security.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "pesquisador@ufape.edu.br";

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private MailService mailService;
    @Mock private AuditService auditService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RateLimiter rateLimiter;

    @InjectMocks
    private AuthService authService;

    private PasswordResetToken token;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setName("Pesquisador");
        token = new PasswordResetToken();
        token.setUser(user);
        token.setToken("123456");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
    }

    @Test
    void correctPinIsAccepted() {
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.of(token));

        assertThatCode(() -> authService.verifyPin(EMAIL, "123456")).doesNotThrowAnyException();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void wrongPinCountsAnAttempt() {
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyPin(EMAIL, "000000"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Código inválido ou não encontrado.");

        assertThat(token.getAttempts()).isEqualTo(1);
        verify(tokenRepository).save(token);
    }

    @Test
    void pinIsDiscardedAfterMaxAttempts() {
        token.setAttempts(AuthService.MAX_PIN_ATTEMPTS - 1);
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyPin(EMAIL, "000000"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("O código expirou");

        verify(tokenRepository).delete(token);
    }

    @Test
    void discardedPinCannotResetPasswordEvenWithTheRightCode() {
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequestDTO(EMAIL, "123456", "Nova@Senha1")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Código inválido.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void expiredPinIsRejected() {
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.verifyPin(EMAIL, "123456"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("O código expirou");
    }

    @Test
    void pinValidationIsRateLimitedByIp() {
        when(tokenRepository.findByUser_Email(EMAIL)).thenReturn(Optional.of(token));

        authService.verifyPin(EMAIL, "123456");

        verify(rateLimiter).check(eq("pin:ip:unknown"), anyInt(), any());
    }

    @Test
    void forgotPasswordGeneratesSixDigitPinAndResetsAttempts() {
        User user = token.getUser();
        token.setAttempts(3);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        authService.forgotPassword(new ForgotPasswordRequestDTO(EMAIL));

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());
        assertThat(saved.getValue().getToken()).matches("\\d{6}");
        assertThat(saved.getValue().getAttempts()).isZero();
        verify(mailService).sendPasswordResetEmail(eq(EMAIL), anyString(), eq(saved.getValue().getToken()));
        verify(rateLimiter).check(eq("forgot:email:" + EMAIL), anyInt(), any());
    }
}
