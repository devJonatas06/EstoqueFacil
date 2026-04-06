package com.example.EstoqueFacil.dto.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com os dados completos de uma categoria")
public class CategoryResponseDTO {

    @Schema(description = "ID único da categoria", example = "1")
    private Long id;

    @Schema(description = "Nome da categoria", example = "Eletrônicos")
    private String name;

    @Schema(description = "Descrição da categoria", example = "Produtos eletrônicos em geral")
    private String description;

    @Schema(description = "Status da categoria (ativa/inativa)", example = "true")
    private Boolean active;

    @Schema(description = "Quantidade de produtos associados a esta categoria", example = "15")
    private Integer productCount;

    @Schema(description = "Data de criação da categoria", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-06-20T14:45:00")
    private LocalDateTime updatedAt;
}