package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.auth.LoginRequestDto;
import com.example.EstoqueFacil.dto.auth.RegisterRequestDto;
import com.example.EstoqueFacil.dto.auth.ResponseDto;
import com.example.EstoqueFacil.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints públicos para registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza login no sistema e retorna um token JWT para ser usado nas requisições protegidas.\n\n" +
                    "**Regras de negócio:**\n" +
                    "- Após 5 tentativas de login falhas, a conta é temporariamente bloqueada\n" +
                    "- Token JWT expira em 2 horas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso - Token JWT gerado"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta bloqueada", content = @Content),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login - Aguarde alguns minutos", content = @Content)
    })
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
        log.info("Tentativa de login - Email: {}", body.email());
        ResponseDto response = authService.login(body);
        log.info("Login realizado com sucesso - Email: {}", body.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta no sistema.\n\n" +
                    "**Regras de negócio:**\n" +
                    "- Email deve ser único\n" +
                    "- Senha deve ter no mínimo 8 caracteres\n" +
                    "- Senha não pode estar na blacklist de senhas comuns"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso - Token JWT gerado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (email duplicado, senha fraca, etc.)", content = @Content)
    })
    public ResponseEntity<ResponseDto> register(@RequestBody RegisterRequestDto body) {
        log.info("Tentativa de registro - Email: {}", body.email());
        ResponseDto response = authService.register(body);
        log.info("Registro realizado com sucesso - Email: {}", body.email());
        return ResponseEntity.ok(response);
    }
}