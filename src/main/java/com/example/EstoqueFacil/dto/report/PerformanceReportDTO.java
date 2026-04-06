package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Relatório de performance do estoque")
public class PerformanceReportDTO {

    @Schema(description = "Produtos encalhados (sem venda por período)")
    private List<InactiveProductDTO> stagnantProducts;

    @Schema(description = "Produtos com alta saída (mais vendidos)")
    private List<BestSellingProductDTO> highTurnoverProducts;

    @Schema(description = "Taxa de giro de estoque por produto")
    private List<ProductTurnoverDTO> turnoverRate;

    @Data
    @Builder
    @Schema(description = "Taxa de giro de um produto específico")
    public static class ProductTurnoverDTO {
        @Schema(description = "ID do produto", example = "1")
        private Long productId;

        @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
        private String productName;

        @Schema(description = "Total de unidades vendidas", example = "500")
        private Integer totalSold;

        @Schema(description = "Estoque médio no período", example = "50")
        private Integer averageStock;

        @Schema(description = "Taxa de giro (vendas / estoque médio)", example = "10.0")
        private Double turnoverRate;
    }
}