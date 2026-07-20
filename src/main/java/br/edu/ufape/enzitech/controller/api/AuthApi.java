package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.ForgotPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.request.VerifyPinRequestDTO;
import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.request.ResetPasswordRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints públicos para login e geração de tokens")
public interface AuthApi {

    @Operation(summary = "Login de Usuário", description = "Valida as credenciais e devolve um token JWT.")
    @PostMapping("/login")
    ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto);

    @Operation(summary = "Esqueci a Senha", description = "Gera um token e envia um e-mail de recuperação.")
    @PostMapping("/forgot-password")
    ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO dto);

    @Operation(summary = "Redefinir Senha", description = "Valida o token e atualiza a senha.")
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO dto);

    @Operation(summary = "Verificar PIN", description = "Verifica a validade do código de recuperação.")
    @PostMapping("/verify-pin")
    ResponseEntity<Void> verifyPin(@RequestBody @Valid VerifyPinRequestDTO dto);
}