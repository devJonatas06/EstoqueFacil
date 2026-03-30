package com.example.EstoqueFacil.dto.product;

import java.math.BigDecimal;

public interface ProductSalesDTO {

    Long getProductId();
    String getProductName();
    String getBarcode();
    Integer getTotalSold();
    BigDecimal getTotalRevenue();
}