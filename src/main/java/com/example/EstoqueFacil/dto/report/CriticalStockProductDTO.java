package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CriticalStockProductDTO {
    private Long productId;
    private String name;
    private String barcode;
    private Integer currentStock;
    private Integer minimumStock;
    private Integer deficit;
    private Integer daysBelowMinimum;
}