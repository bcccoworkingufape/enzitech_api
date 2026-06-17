package br.edu.ufape.enzitech.controller;

import br.edu.ufape.enzitech.controller.api.AuthApi;
import br.edu.ufape.enzitech.dto.request.ForgotPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.request.ResetPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import br.edu.ufape.enzitech.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*/**")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponseDTO> login(LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Override
    public ResponseEntity<Void> forgotPassword(ForgotPasswordRequestDTO dto) {
        authService.forgotPassword(dto);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(ResetPasswordRequestDTO dto) {
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }
}