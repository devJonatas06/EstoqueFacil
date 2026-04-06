package com.example.EstoqueFacil.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação contendo token JWT")
public record ResponseDto(
        @Schema(description = "Nome do usuário autenticado", example = "João Silva")
        String name,

        @Schema(description = "Token JWT para acesso aos endpoints protegidos. Deve ser enviado no header Authorization: Bearer {token}",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb2dpbi1hdXRoLWFwaSIsInN1YiI6ImFkbWluQGVzdG9xdWUuY29tIiwiZXhwIjoxNzM1MTg5NTIzfQ.abc123")
        String token
) {}