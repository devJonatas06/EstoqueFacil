package com.example.EstoqueFacil.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Filtros avançados para busca de produtos")
public class ProductFilterDTO {

    @Schema(description = "Filtrar por nome do produto (busca parcial)", example = "Smartphone")
    private String name;

    @Schema(description = "Filtrar por código de barras exato", example = "7891234567890")
    private String barcode;

    @Schema(description = "Filtrar por ID da categoria", example = "1")
    private Long categoryId;

    @Schema(description = "Preço mínimo para filtro", example = "500.00")
    private BigDecimal minPrice;

    @Schema(description = "Preço máximo para filtro", example = "5000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Status do estoque", example = "BAIXO", allowableValues = {"OK", "BAIXO", "CRÍTICO"})
    private String stockStatus;

    @Schema(description = "Status do produto (ativo/inativo)", example = "true")
    private Boolean active;
}