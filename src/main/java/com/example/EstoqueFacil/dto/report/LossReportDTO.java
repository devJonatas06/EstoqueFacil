package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LossReportDTO {
    private List<ExpiredBatchDTO> expiredProducts;
    private BigDecimal totalEstimatedLoss;
    private Integer totalExpiredUnits;
}