package com.example.EstoqueFacil.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta com os dados completos de um produto")
public class ProductResponseDTO {

    @Schema(description = "ID único do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String name;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Descrição do produto", example = "Smartphone 256GB, 8GB RAM")
    private String description;

    @Schema(description = "Fabricante", example = "Samsung")
    private String maker;

    @Schema(description = "Preço de custo", example = "2500.00")
    private BigDecimal costPrice;

    @Schema(description = "Preço de venda", example = "3500.00")
    private BigDecimal salePrice;

    @Schema(description = "Margem de lucro (preço venda - preço custo)", example = "1000.00")
    private BigDecimal profitMargin;

    @Schema(description = "Estoque mínimo configurado", example = "10")
    private Integer minimumStock;

    @Schema(description = "Quantidade atual em estoque", example = "25")
    private Integer currentStock;

    @Schema(description = "Status do estoque", example = "OK", allowableValues = {"OK", "BAIXO", "CRÍTICO"})
    private String stockStatus;

    @Schema(description = "Nome da categoria", example = "Eletrônicos")
    private String categoryName;

    @Schema(description = "ID da categoria", example = "1")
    private Long categoryId;

    @Schema(description = "Status do produto (ativo/inativo)", example = "true")
    private Boolean active;

    @Schema(description = "Data de criação", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-06-20T14:45:00")
    private LocalDateTime updatedAt;
}