package com.example.EstoqueFacil.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para criação ou atualização de uma categoria")
public class CategoryRequestDTO {

    @Schema(description = "Nome da categoria", example = "Eletrônicos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    private String name;

    @Schema(description = "Descrição detalhada da categoria", example = "Produtos eletrônicos em geral")
    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    private String description;
}