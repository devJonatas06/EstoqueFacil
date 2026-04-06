package com.example.EstoqueFacil.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Projeção de vendas de produto para relatórios")
public interface ProductSalesDTO {

    @Schema(description = "ID do produto", example = "1")
    Long getProductId();

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    String getProductName();

    @Schema(description = "Código de barras", example = "7891234567890")
    String getBarcode();

    @Schema(description = "Quantidade total vendida", example = "150")
    Integer getTotalSold();

    @Schema(description = "Receita total gerada", example = "525000.00")
    BigDecimal getTotalRevenue();
}