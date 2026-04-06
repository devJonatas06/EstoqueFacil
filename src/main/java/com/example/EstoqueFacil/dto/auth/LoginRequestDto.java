package com.example.EstoqueFacil.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticação do usuário")
public record LoginRequestDto(
        @Schema(description = "Email do usuário cadastrado no sistema", example = "admin@estoque.com")
        @NotBlank @Email String email,

        @Schema(description = "Senha do usuário", example = "Admin@123")
        @NotBlank String password
) {}