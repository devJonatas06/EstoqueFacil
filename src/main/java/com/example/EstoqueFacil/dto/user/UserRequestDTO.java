package com.example.EstoqueFacil.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para criação/atualização de usuário")
public class UserRequestDTO {

    @Schema(description = "Nome completo do usuário", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Schema(description = "Email do usuário (deve ser único)", example = "joao@empresa.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "Senha do usuário - mínimo 6 caracteres, deve conter número, letra maiúscula, letra minúscula e caractere especial",
            example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Senha deve conter pelo menos: 1 número, 1 letra maiúscula, 1 letra minúscula e 1 caractere especial"
    )
    private String password;
}