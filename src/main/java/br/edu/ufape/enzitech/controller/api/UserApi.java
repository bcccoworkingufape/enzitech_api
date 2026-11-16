package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.PromoteAdminRequestDTO;
import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import br.edu.ufape.enzitech.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints para gestão de utilizadores")
public interface UserApi {

    @Operation(summary = "Listar usuários", description = "Devolve todos os usuários paginados. Requer ADMIN.")
    @GetMapping
    ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable);

    @Operation(summary = "Meu perfil", description = "Devolve os dados do usuário autenticado.")
    @GetMapping("/me")
    ResponseEntity<UserResponseDTO> getMe(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "Procurar usuário", description = "Devolve um usuário pelo seu ID. Requer ADMIN.")
    @GetMapping("/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id);

    @Operation(summary = "Criar usuário", description = "Regista um novo usuário.")
    @PostMapping
    ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto);

    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente. Permitido para o próprio usuário ou ADMIN.")
    @PutMapping("/{id}")
    ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UserRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Eliminar usuário", description = "Elimina um usuário da base de dados. Requer ADMIN.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable UUID id);

    @Operation(summary = "Promover usuário a ADMIN", description = "Concede o papel de ADMIN ao usuário do email informado. Requer que o solicitante já seja ADMIN.")
    @PatchMapping("/promote-admin")
    ResponseEntity<UserResponseDTO> promoteToAdmin(
            @RequestBody @Valid PromoteAdminRequestDTO dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Excluir minha conta", description = "Remove definitivamente a conta do usuário autenticado, junto com os experimentos e tratamentos criados por ele. Ação irreversível.")
    @DeleteMapping("/me")
    ResponseEntity<Void> deleteMyAccount(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "Foto de perfil", description = "Devolve a foto de perfil do usuário. Permitido para o próprio usuário ou ADMIN. "
            + "Com o parâmetro v (versão = avatarUpdatedAt), a resposta é cacheável indefinidamente pelo navegador.")
    @GetMapping("/{id}/avatar")
    ResponseEntity<byte[]> getAvatar(
            @PathVariable UUID id,
            @RequestParam(required = false) String v,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Atualizar minha foto de perfil", description = "Envia (multipart, campo file) uma foto JPEG, PNG ou WebP de até 256 KB e 1024x1024 pixels. "
            + "O cliente deve redimensionar a imagem antes do envio.")
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UserResponseDTO> updateMyAvatar(
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "Remover minha foto de perfil", description = "Remove a foto de perfil do usuário autenticado.")
    @DeleteMapping("/me/avatar")
    ResponseEntity<UserResponseDTO> deleteMyAvatar(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
