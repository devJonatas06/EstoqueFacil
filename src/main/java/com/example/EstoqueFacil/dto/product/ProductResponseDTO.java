package com.example.EstoqueFacil.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String barcode;
    private String description;
    private String maker;

    private BigDecimal costPrice;
    private BigDecimal salePrice;
    


    private BigDecimal profitMargin;

    private Integer minimumStock;
    private Integer currentStock;
    

    private String stockStatus;

    private String categoryName;
    private Long categoryId;

    private Boolean active;
    

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}