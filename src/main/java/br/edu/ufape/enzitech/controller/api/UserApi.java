package br.edu.ufape.enzitech.controller.api;

import br.edu.ufape.enzitech.dto.request.UserRequestDTO;
import br.edu.ufape.enzitech.dto.response.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints para gestão de utilizadores")
public interface UserApi {

    @Operation(summary = "Listar Utilizadores", description = "Devolve todos os utilizadores paginados.")
    @GetMapping
    ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable);

    @Operation(summary = "Procurar Utilizador", description = "Devolve um utilizador pelo seu ID.")
    @GetMapping("/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id);

    @Operation(summary = "Criar Utilizador", description = "Regista um novo utilizador na base de dados.")
    @PostMapping
    ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto);

    @Operation(summary = "Atualizar Utilizador", description = "Atualiza os dados de um utilizador existente.")
    @PutMapping("/{id}")
    ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody @Valid UserRequestDTO dto);

    @Operation(summary = "Eliminar Utilizador", description = "Elimina um utilizador da base de dados.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable UUID id);
}