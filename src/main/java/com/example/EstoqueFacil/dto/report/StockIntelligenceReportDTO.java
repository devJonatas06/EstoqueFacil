package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockIntelligenceReportDTO {
    private List<CriticalStockProductDTO> criticalProducts;
    private List<StockBreakdownPredictionDTO> breakdownPredictions;
    private List<DaysBelowMinimumDTO> daysBelowMinimum;
    
    @Data
    @Builder
    public static class StockBreakdownPredictionDTO {
        private Long productId;
        private String productName;
        private Integer currentStock;
        private Integer estimatedDailySales;
        private Integer estimatedDaysToBreakdown;
        private String riskLevel; // ALTO, MÉDIO, BAIXO
    }
    
    @Data
    @Builder
    public static class DaysBelowMinimumDTO {
        private Long productId;
        private String productName;
        private Integer currentStock;
        private Integer minimumStock;
        private Integer daysBelowMinimum;
    }
}