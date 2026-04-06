package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detalhes completos de todos os alertas do sistema")
public class AlertDetailDTO {

    @Schema(description = "Lista de produtos com estoque abaixo do mínimo")
    private List<LowStockProductDTO> lowStockProducts;

    @Schema(description = "Lista de produtos inativos/parados")
    private List<InactiveProductDTO> inactiveProducts;

    @Schema(description = "Lista de lotes próximos ao vencimento")
    private List<ExpiringBatchDTO> expiringBatches;

    @Schema(description = "Lista de lotes já vencidos")
    private List<ExpiredBatchDTO> expiredBatches;

    @Schema(description = "Lista de produtos com estoque crítico")
    private List<CriticalStockProductDTO> criticalStockProducts;
}