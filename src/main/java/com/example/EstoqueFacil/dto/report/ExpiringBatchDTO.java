package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Lote de produto próximo ao vencimento")
public class ExpiringBatchDTO {

    @Schema(description = "ID do lote", example = "1")
    private Long batchId;

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String productName;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade no lote", example = "100")
    private Integer quantity;

    @Schema(description = "Data de vencimento", example = "2024-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Dias até o vencimento", example = "45")
    private Integer daysToExpire;

    @Schema(description = "Status do lote", example = "ATENÇÃO", allowableValues = {"URGENTE", "ATENÇÃO", "OK"})
    private String status;

    @Schema(description = "Valor total do lote", example = "12500.00")
    private BigDecimal costValue;
}