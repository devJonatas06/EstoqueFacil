package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.user.UserRequestDTO;
import com.example.EstoqueFacil.dto.user.UserResponseDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
//@SecurityRequirement(name = "bearer-auth")
public class UserController {

    private final UserService userService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo usuário (funcionário)", description = "Apenas ADMIN pode criar usuários")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO requestDTO) {
        log.info("ADMIN criando novo usuário - Email: {}, Nome: {}", requestDTO.getEmail(), requestDTO.getName());

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.create(requestDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário criado com sucesso em {}ms. ID: {}, Email: {}, Role: {}",
                duration, response.getId(), response.getEmail(), response.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id) {

        log.info("Buscando usuário por ID: {}", id);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.findById(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário encontrado em {}ms. ID: {}, Email: {}, Ativo: {}",
                duration, response.getId(), response.getEmail(), response.getActive());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Buscar usuário por email")
    public ResponseEntity<UserResponseDTO> findByEmail(
            @PathVariable @Email(message = "Email inválido") @NotBlank(message = "Email não pode ser vazio") String email) {

        log.info("Buscando usuário por email: {}", email);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.findByEmail(email);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário encontrado em {}ms. ID: {}, Nome: {}", duration, response.getId(), response.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        log.info("Listando todos os usuários");

        long startTime = System.currentTimeMillis();
        List<UserResponseDTO> response = userService.findAll();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Listagem de usuários concluída em {}ms. Total de usuários: {}", duration, response.size());

        // ✅ Log da quantidade de usuários por role
        long adminCount = response.stream().filter(u -> u.getRoles().contains("ROLE_ADMIN")).count();
        long employeeCount = response.stream().filter(u -> u.getRoles().contains("ROLE_EMPLOYEE")).count();

        log.info("Distribuição de roles: ADMIN={}, EMPLOYEE={}", adminCount, employeeCount);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário")
    public ResponseEntity<Void> deactivate(
            @PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id) {

        log.warn("ADMIN desativando usuário ID: {}", id);

        long startTime = System.currentTimeMillis();
        userService.deactivate(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Usuário ID: {} desativado com sucesso em {}ms", id, duration);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar role do usuário (ADMIN/EMPLOYEE)")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable @Min(value = 1, message = "ID do usuário deve ser maior que 0") Long id,
            @RequestParam @Pattern(regexp = "^(ADMIN|EMPLOYEE)$", message = "Role deve ser ADMIN ou EMPLOYEE") String role) {

        log.warn("ADMIN alterando role do usuário ID: {} para: {}", id, role);

        long startTime = System.currentTimeMillis();
        UserResponseDTO response = userService.changeRole(id, role);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Role do usuário ID: {} alterada para {} em {}ms. Novas roles: {}",
                id, role, duration, response.getRoles());

        return ResponseEntity.ok(response);
    }
}