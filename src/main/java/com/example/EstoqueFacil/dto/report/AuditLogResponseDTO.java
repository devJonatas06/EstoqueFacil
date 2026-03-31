package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponseDTO {

    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private Long userId;
    private String userEmail;
    private String oldValue;
    private String newValue;
    private String details;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}