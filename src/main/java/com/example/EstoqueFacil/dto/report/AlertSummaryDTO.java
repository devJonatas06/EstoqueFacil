package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resumo de alertas com contadores")
public class AlertSummaryDTO {

    @Schema(description = "Quantidade de produtos com estoque baixo", example = "5")
    private Integer lowStockCount;

    @Schema(description = "Quantidade de produtos inativos", example = "3")
    private Integer inactiveProductsCount;

    @Schema(description = "Quantidade de lotes próximos ao vencimento", example = "8")
    private Integer expiringSoonCount;

    @Schema(description = "Quantidade de lotes vencidos", example = "2")
    private Integer expiredCount;

    @Schema(description = "Quantidade de produtos com estoque crítico", example = "4")
    private Integer criticalStockCount;

    @Schema(description = "Data e hora da geração do resumo", example = "2024-01-15T10:30:00")
    private LocalDateTime generatedAt;
}