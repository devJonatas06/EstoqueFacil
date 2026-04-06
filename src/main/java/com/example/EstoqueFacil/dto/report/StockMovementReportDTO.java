package com.example.EstoqueFacil.dto.report;

import com.example.EstoqueFacil.entity.StockMovementType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Relatório de movimentação de estoque")
public class StockMovementReportDTO {

    @Schema(description = "ID da movimentação", example = "1")
    private Long movementId;

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String productName;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade movimentada", example = "10")
    private Integer quantity;

    @Schema(description = "Tipo de movimentação", example = "SALE", allowableValues = {"ENTRY", "SALE", "LOSS"})
    private StockMovementType type;

    @Schema(description = "Preço unitário", example = "3500.00")
    private BigDecimal unitPrice;

    @Schema(description = "Valor total da movimentação", example = "35000.00")
    private BigDecimal totalValue;

    @Schema(description = "Nome do usuário responsável", example = "admin@estoque.com")
    private String userName;

    @Schema(description = "Observação da movimentação", example = "Venda para cliente João")
    private String observation;

    @Schema(description = "Data da movimentação", example = "2024-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime movementDate;
}