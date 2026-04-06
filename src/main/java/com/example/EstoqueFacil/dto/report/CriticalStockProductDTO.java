package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Produto com estoque crítico")
public class CriticalStockProductDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String name;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade atual em estoque", example = "3")
    private Integer currentStock;

    @Schema(description = "Estoque mínimo configurado", example = "10")
    private Integer minimumStock;

    @Schema(description = "Déficit em relação ao mínimo", example = "7")
    private Integer deficit;

    @Schema(description = "Dias abaixo do estoque mínimo", example = "5")
    private Integer daysBelowMinimum;
}