package br.edu.ufape.enzitech.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.enzitech.dto.request.ForgotPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.request.ResetPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.exception.InvalidCredentialsException;
import br.edu.ufape.enzitech.model.PasswordResetToken;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.model.enums.AuditAction;
import br.edu.ufape.enzitech.repository.PasswordResetTokenRepository;
import br.edu.ufape.enzitech.repository.UserRepository;
import br.edu.ufape.enzitech.security.ClientIp;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.security.JwtService;
import br.edu.ufape.enzitech.security.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    static final int MAX_PIN_ATTEMPTS = 5;

    private static final String INVALID_CODE = "Código inválido.";
    private static final String INVALID_OR_NOT_FOUND_CODE = "Código inválido ou não encontrado.";
    private static final String EXPIRED_CODE = "O código expirou. Por favor, solicite um novo.";
    private static final Duration FIFTEEN_MINUTES = Duration.ofMinutes(15);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        String emailKey = "login:email:" + normalize(dto.email());
        rateLimiter.check("login:ip:" + ClientIp.current(), 30, FIFTEEN_MINUTES);
        rateLimiter.check(emailKey, 10, FIFTEEN_MINUTES);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (BadCredentialsException e) {
            log.warn("Tentativa de login falhou: email={}", dto.email());
            auditService.record(AuditAction.LOGIN_FALHOU, HttpStatus.BAD_REQUEST.value(), dto.email(),
                    e.getClass().getSimpleName());
            throw new InvalidCredentialsException();
        }

        rateLimiter.reset(emailKey);
        User user = userRepository.findByEmail(dto.email()).orElseThrow();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);
        log.info("Login bem-sucedido: email={}", user.getEmail());
        return new AuthResponseDTO(jwtToken, UserResponseDTO.fromEntity(user));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        rateLimiter.check("forgot:ip:" + ClientIp.current(), 10, Duration.ofHours(1));
        rateLimiter.check("forgot:email:" + normalize(dto.email()), 3, FIFTEEN_MINUTES);

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail não está cadastrado em nossa base de dados."));

        String pinCode = generatePin();
        PasswordResetToken resetToken = tokenRepository.findByUser(user).orElse(new PasswordResetToken());
        resetToken.setToken(pinCode);
        resetToken.setUser(user);
        resetToken.setAttempts(0);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        if (resetToken.getId() == null) {
            resetToken.setCreatedAt(LocalDateTime.now());
        }
        resetToken.setUpdatedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        mailService.sendPasswordResetEmail(user.getEmail(), user.getName(), pinCode);
        log.info("PIN de recuperação de senha enviado: email={}", user.getEmail());
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void resetPassword(ResetPasswordRequestDTO dto) {
        PasswordResetToken resetToken = validatePin(dto.email(), dto.token(), INVALID_CODE);

        User user = resetToken.getUser();
        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A nova senha não pode ser igual à senha atual.");
        }

        LocalDateTime changedAt = LocalDateTime.now();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setCredentialsUpdatedAt(changedAt);
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        log.info("Senha redefinida com sucesso: email={}", user.getEmail());

        try {
            mailService.sendPasswordChangedEmail(user.getEmail(), user.getName(), changedAt);
        } catch (RuntimeException e) {
            log.error("Falha ao enviar e-mail de senha alterada: email={}", user.getEmail(), e);
        }
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void verifyPin(String email, String token) {
        validatePin(email, token, INVALID_OR_NOT_FOUND_CODE);
    }

    private PasswordResetToken validatePin(String email, String pin, String invalidMessage) {
        rateLimiter.check("pin:ip:" + ClientIp.current(), 20, FIFTEEN_MINUTES);

        if (email == null || pin == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidMessage);
        }

        PasswordResetToken resetToken = tokenRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidMessage));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EXPIRED_CODE);
        }

        boolean matches = MessageDigest.isEqual(
                pin.trim().getBytes(StandardCharsets.UTF_8),
                resetToken.getToken().getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            int attempts = resetToken.getAttempts() + 1;
            auditService.record(AuditAction.PIN_INVALIDO, HttpStatus.BAD_REQUEST.value(), email,
                    "Tentativa " + attempts + " de " + MAX_PIN_ATTEMPTS);

            if (attempts >= MAX_PIN_ATTEMPTS) {
                tokenRepository.delete(resetToken);
                log.warn("PIN descartado após {} tentativas inválidas: email={}", attempts, email);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EXPIRED_CODE);
            }

            resetToken.setAttempts(attempts);
            tokenRepository.save(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidMessage);
        }

        return resetToken;
    }

    private String generatePin() {
        String pin;
        do {
            pin = String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (tokenRepository.existsByToken(pin));
        return pin;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
