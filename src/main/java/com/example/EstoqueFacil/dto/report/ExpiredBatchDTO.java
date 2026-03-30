package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpiredBatchDTO {

    private Long batchId;
    private Long productId;
    private String productName;
    private String barcode;
    private Integer quantity;
    private LocalDate expirationDate;
    private Integer daysExpired;
    private BigDecimal estimatedLoss;
    private String status;
}