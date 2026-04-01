package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceReportDTO {
    private List<InactiveProductDTO> stagnantProducts;
    private List<BestSellingProductDTO> highTurnoverProducts;
    private List<ProductTurnoverDTO> turnoverRate;
    
    @Data
    @Builder
    public static class ProductTurnoverDTO {
        private Long productId;
        private String productName;
        private Integer totalSold;
        private Integer averageStock;
        private Double turnoverRate; // giro = vendas / estoque médio
    }
}