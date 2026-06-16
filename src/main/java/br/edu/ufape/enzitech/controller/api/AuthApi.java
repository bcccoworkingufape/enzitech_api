package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.LoginRequestDTO;
import br.edu.ufape.enzitech.dto.response.AuthResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints públicos para login e geração de tokens")
public interface AuthApi {

    @Operation(summary = "Login de Usuário", description = "Valida as credenciais e devolve um token JWT.")
    @PostMapping("/login")
    ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto);
}