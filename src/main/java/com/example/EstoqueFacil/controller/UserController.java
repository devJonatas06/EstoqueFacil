package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;
import com.example.EstoqueFacil.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários do sistema")
@SecurityRequirement(name = "bearer-auth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar novo usuário",
            description = "Cria um novo usuário no sistema com role EMPLOYEE.\n\n" +
                    "**Regras de negócio:**\n" +
                    "- Apenas ADMIN pode criar usuários\n" +
                    "- Email deve ser único\n" +
                    "- Senha deve ter no mínimo 6 caracteres\n" +
                    "- Usuário é criado com role EMPLOYEE por padrão\n\n" +
                    "**Exemplo de requisição:**\n```json\n{\n  \"name\": \"João Silva\",\n  \"email\": \"joao@empresa.com\",\n  \"password\": \"Joao@123\"\n}\n```"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (email duplicado, senha fraca)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Necessário role ADMIN", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO requestDTO) {
        log.info("Usuário - ADMIN criando. Email: {}, Nome: {}", requestDTO.getEmail(), requestDTO.getName());

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.create(requestDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário - Criado com sucesso. ID: {}, Email: {}, Roles: {}, Tempo: {}ms",
                response.getId(), response.getEmail(), response.getRoles(), duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os detalhes de um usuário específico pelo seu ID. Apenas ADMIN pode acessar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> findById(@PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id) {
        log.info("Usuário - Busca por ID: {}", id);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.findById(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário - Encontrado. ID: {}, Email: {}, Ativo: {}, Tempo: {}ms",
                response.getId(), response.getEmail(), response.getActive(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Buscar usuário por email",
            description = "Retorna os detalhes de um usuário específico pelo seu email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Email inválido", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> findByEmail(
            @PathVariable @Email(message = "Email inválido") @NotBlank(message = "Email não pode ser vazio") String email) {

        log.info("Usuário - Busca por email: {}", email);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.findByEmail(email);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário - Encontrado por email. ID: {}, Nome: {}, Tempo: {}ms",
                response.getId(), response.getName(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com todos os usuários cadastrados no sistema. Apenas ADMIN pode acessar."
    )
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        long startTime = System.currentTimeMillis();
        List<UserResponseDTO> response = userService.findAll();
        long duration = System.currentTimeMillis() - startTime;

        long adminCount = response.stream().filter(u -> u.getRoles().contains("ROLE_ADMIN")).count();
        long employeeCount = response.stream().filter(u -> u.getRoles().contains("ROLE_EMPLOYEE")).count();

        log.info("Usuário - Listagem concluída. Total: {}, ADMIN: {}, EMPLOYEE: {}, Tempo: {}ms",
                response.size(), adminCount, employeeCount, duration);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar usuário",
            description = "Realiza desativação lógica do usuário (soft delete). O usuário não é removido do banco, apenas marcado como inativo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Void> deactivate(@PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id) {
        log.warn("Usuário - ADMIN desativando ID: {}", id);

        long startTime = System.currentTimeMillis();
        userService.deactivate(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário - Desativado com sucesso. ID: {}, Tempo: {}ms", id, duration);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Alterar role do usuário",
            description = "Altera a permissão/role de um usuário (ADMIN ou EMPLOYEE). Apenas ADMIN pode executar esta ação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Role inválida (deve ser ADMIN ou EMPLOYEE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id,
            @RequestParam @Pattern(regexp = "^(ADMIN|EMPLOYEE)$", message = "Role deve ser ADMIN ou EMPLOYEE") String role) {

        log.warn("Usuário - ADMIN alterando role. ID: {}, Nova role: {}", id, role);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.changeRole(id, role);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário - Role alterada com sucesso. ID: {}, Novas roles: {}, Tempo: {}ms",
                id, response.getRoles(), duration);
        return ResponseEntity.ok(response);
    }
}