package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Relatório de inteligência de estoque")
public class StockIntelligenceReportDTO {

    @Schema(description = "Produtos com estoque crítico")
    private List<CriticalStockProductDTO> criticalProducts;

    @Schema(description = "Previsão de ruptura de estoque")
    private List<StockBreakdownPredictionDTO> breakdownPredictions;

    @Schema(description = "Produtos com dias abaixo do mínimo")
    private List<DaysBelowMinimumDTO> daysBelowMinimum;

    @Data
    @Builder
    @Schema(description = "Previsão de ruptura de um produto")
    public static class StockBreakdownPredictionDTO {
        @Schema(description = "ID do produto", example = "1")
        private Long productId;

        @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
        private String productName;

        @Schema(description = "Estoque atual", example = "25")
        private Integer currentStock;

        @Schema(description = "Média de vendas por dia", example = "5")
        private Integer estimatedDailySales;

        @Schema(description = "Dias estimados até ruptura", example = "5")
        private Integer estimatedDaysToBreakdown;

        @Schema(description = "Nível de risco", example = "ALTO", allowableValues = {"ALTO", "MÉDIO", "BAIXO"})
        private String riskLevel;
    }

    @Data
    @Builder
    @Schema(description = "Produto com dias abaixo do mínimo")
    public static class DaysBelowMinimumDTO {
        @Schema(description = "ID do produto", example = "1")
        private Long productId;

        @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
        private String productName;

        @Schema(description = "Estoque atual", example = "5")
        private Integer currentStock;

        @Schema(description = "Estoque mínimo", example = "10")
        private Integer minimumStock;

        @Schema(description = "Dias abaixo do mínimo", example = "7")
        private Integer daysBelowMinimum;
    }
}