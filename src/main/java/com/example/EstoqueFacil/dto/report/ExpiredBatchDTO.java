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
@Schema(description = "Lote de produto vencido")
public class ExpiredBatchDTO {

    @Schema(description = "ID do lote", example = "1")
    private Long batchId;

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String productName;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade vencida", example = "50")
    private Integer quantity;

    @Schema(description = "Data de vencimento", example = "2024-01-15")
    private LocalDate expirationDate;

    @Schema(description = "Dias desde o vencimento", example = "30")
    private Integer daysExpired;

    @Schema(description = "Prejuízo estimado", example = "12500.00")
    private BigDecimal estimatedLoss;

    @Schema(description = "Status do lote", example = "EXPIRED")
    private String status;
}