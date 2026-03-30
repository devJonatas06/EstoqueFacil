package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertDetailDTO {

    private List<LowStockProductDTO> lowStockProducts;
    private List<InactiveProductDTO> inactiveProducts;
    private List<ExpiringBatchDTO> expiringBatches;
    private List<ExpiredBatchDTO> expiredBatches;
    private List<CriticalStockProductDTO> criticalStockProducts;
}