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
public class ProfitReportDTO {

    private BigDecimal totalProfit;
    private LocalDateTime start;
    private LocalDateTime end;

    private Map<String, BigDecimal> profitByProduct;


    private Integer totalItemsSold;

    private BigDecimal averageTicket;

    private String topProfitProduct;
}