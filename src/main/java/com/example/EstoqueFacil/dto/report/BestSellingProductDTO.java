package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Produto mais vendido ou com melhor performance")
public class BestSellingProductDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String productName;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade total vendida", example = "150")
    private Integer totalSold;

    @Schema(description = "Receita total gerada", example = "525000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Lucro total gerado", example = "150000.00")
    private BigDecimal profit;
}