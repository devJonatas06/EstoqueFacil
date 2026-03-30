package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveProductDTO {
    private Long productId;
    private String name;
    private String barcode;
    private Integer currentStock;
    private LocalDateTime lastMovementDate;
    private Integer daysInactive;
}