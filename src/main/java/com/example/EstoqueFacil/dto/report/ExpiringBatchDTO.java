package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpiringBatchDTO {
    private Long batchId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private LocalDate expirationDate;
    private Integer daysToExpire;
    private String status;
}