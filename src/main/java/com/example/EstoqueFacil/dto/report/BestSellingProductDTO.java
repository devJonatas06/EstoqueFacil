package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BestSellingProductDTO {

    private Long productId;
    private String productName;
    private String barcode;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private BigDecimal profit;
}