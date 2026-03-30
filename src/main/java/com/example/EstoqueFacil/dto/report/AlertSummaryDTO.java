package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertSummaryDTO {

    private Integer lowStockCount;
    private Integer inactiveProductsCount;
    private Integer expiringSoonCount;
    private Integer expiredCount;
    private Integer criticalStockCount;
    private LocalDateTime generatedAt;
}