package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Produto sem movimentação por período")
public class InactiveProductDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long productId;

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23")
    private String name;

    @Schema(description = "Código de barras", example = "7891234567890")
    private String barcode;

    @Schema(description = "Quantidade atual em estoque", example = "25")
    private Integer currentStock;

    @Schema(description = "Data da última movimentação", example = "2024-01-15T10:30:00")
    private LocalDateTime lastMovementDate;

    @Schema(description = "Dias sem movimentação", example = "45")
    private Integer daysInactive;
}