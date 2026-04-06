package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Relatório financeiro por período")
public class FinancialReportDTO {

    @Schema(description = "Lucro total no período", example = "150000.00")
    private BigDecimal totalProfit;

    @Schema(description = "Lucro detalhado por produto")
    private Map<String, BigDecimal> profitByProduct;

    @Schema(description = "Produto que mais gerou lucro", example = "Smartphone Galaxy S23")
    private String topProfitProduct;

    @Schema(description = "Data de início do período", example = "2024-01-01T00:00:00")
    private LocalDateTime start;

    @Schema(description = "Data de fim do período", example = "2024-12-31T23:59:59")
    private LocalDateTime end;

    @Schema(description = "Total de itens vendidos", example = "500")
    private Integer totalItemsSold;

    @Schema(description = "Ticket médio por venda", example = "300.00")
    private BigDecimal averageTicket;
}