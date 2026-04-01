package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialReportDTO {
    private BigDecimal totalProfit;
    private Map<String, BigDecimal> profitByProduct;
    private String topProfitProduct;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer totalItemsSold;
    private BigDecimal averageTicket;
}