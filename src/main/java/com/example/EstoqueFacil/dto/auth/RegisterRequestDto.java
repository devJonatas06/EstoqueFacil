package com.example.EstoqueFacil.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registro de novo usuário no sistema")
public record RegisterRequestDto(
        @Schema(description = "Nome completo do usuário", example = "João Silva")
        @NotBlank String name,

        @Schema(description = "Email do usuário (deve ser único no sistema)", example = "joao@empresa.com")
        @NotBlank @Email String email,

        @Schema(description = "Senha do usuário - mínimo 8 caracteres", example = "SenhaForte@123")
        @NotBlank @Size(min = 8, message = "Password must have at least 8 characters") String password
) {}