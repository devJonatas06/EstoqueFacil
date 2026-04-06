package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Relatório de perdas com produtos vencidos")
public class LossReportDTO {

    @Schema(description = "Lista de produtos vencidos")
    private List<ExpiredBatchDTO> expiredProducts;

    @Schema(description = "Prejuízo total estimado", example = "12500.00")
    private BigDecimal totalEstimatedLoss;

    @Schema(description = "Total de unidades vencidas", example = "150")
    private Integer totalExpiredUnits;
}