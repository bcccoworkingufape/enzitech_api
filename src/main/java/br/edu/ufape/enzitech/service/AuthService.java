package br.edu.ufape.enzitech.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufape.enzitech.dto.request.ForgotPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.request.ResetPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.exception.InvalidCredentialsException;
import br.edu.ufape.enzitech.model.PasswordResetToken;
import br.edu.ufape.enzitech.model.User;
import br.edu.ufape.enzitech.repository.PasswordResetTokenRepository;
import br.edu.ufape.enzitech.repository.UserRepository;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import br.edu.ufape.enzitech.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(dto.email()).orElseThrow();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponseDTO(jwtToken, UserResponseDTO.fromEntity(user));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Se o e-mail existir em nossa base de dados, receberá um código."));

        tokenRepository.deleteByUser_Id(user.getId());

        String pinCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(pinCode);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        
        tokenRepository.save(resetToken);

        mailService.sendPasswordResetEmail(user.getEmail(), user.getName(), pinCode);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUser_Email(dto.token(), dto.email())
                .orElseThrow(() -> new RuntimeException("Código inválido."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("O código expirou. Por favor, solicite um novo.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}