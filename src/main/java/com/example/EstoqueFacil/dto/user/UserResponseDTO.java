package com.example.EstoqueFacil.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com dados do usuário")
public class UserResponseDTO {

    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome do usuário", example = "João Silva")
    private String name;

    @Schema(description = "Email do usuário", example = "joao@empresa.com")
    private String email;

    @Schema(description = "Status do usuário (ativo/inativo)", example = "true")
    private Boolean active;

    @Schema(description = "Roles/permissões do usuário", example = "[\"ROLE_EMPLOYEE\"]")
    private Set<String> roles;

    @Schema(description = "Data de criação do usuário", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}