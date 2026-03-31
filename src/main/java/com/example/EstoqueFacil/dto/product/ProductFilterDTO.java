package com.example.EstoqueFacil.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductFilterDTO {

    private String name;
    private String barcode;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String stockStatus; // OK, BAIXO, CRÍTICO
    private Boolean active;
}