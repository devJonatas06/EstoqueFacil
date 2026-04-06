package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Recuperação de Senha", description = "Endpoints para recuperação e redefinição de senha")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar recuperação de senha",
            description = "Envia um link de recuperação de senha para o email informado.\n\n" +
                    "**Regras:**\n" +
                    "- O link expira em 5 minutos\n" +
                    "- Após 5 tentativas, o email é bloqueado temporariamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link de recuperação enviado (se email existir)"),
            @ApiResponse(responseCode = "400", description = "Email inválido ou muitas tentativas", content = @Content)
    })
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        log.info("Reset de senha - Solicitação para email: {}", email);
        ResponseEntity<String> response = passwordResetService.requestReset(email);
        log.info("Reset de senha - Solicitação processada para: {}", email);
        return response;
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Redefinir senha",
            description = "Redefine a senha do usuário usando um token válido.\n\n" +
                    "**Regras:**\n" +
                    "- Token deve ser válido e não expirado (5 minutos)\n" +
                    "- Nova senha deve atender aos critérios de segurança"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido/expirado ou senha fraca", content = @Content)
    })
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        log.info("Reset de senha - Tentativa de redefinição com token");
        ResponseEntity<String> response = passwordResetService.resetPassword(token, newPassword);
        log.info("Reset de senha - Processo finalizado");
        return response;
    }
}