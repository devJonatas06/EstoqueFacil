package com.example.EstoqueFacil.dto.report;

import com.example.EstoqueFacil.entity.StockMovementType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockMovementReportDTO {

    private Long movementId;
    private Long productId;
    private String productName;
    private String barcode;
    private Integer quantity;
    private StockMovementType type;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private String userName;
    private String observation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime movementDate;
}