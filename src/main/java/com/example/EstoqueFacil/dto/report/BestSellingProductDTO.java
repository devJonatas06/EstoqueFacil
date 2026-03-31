package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BestSellingProductDTO {
    private Long productId;
    private String productName;
    private String barcode;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal profit;
}