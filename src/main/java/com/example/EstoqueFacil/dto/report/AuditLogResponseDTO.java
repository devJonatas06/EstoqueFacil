package com.example.EstoqueFacil.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Log de auditoria do sistema")
public class AuditLogResponseDTO {

    @Schema(description = "ID do log", example = "1")
    private Long id;

    @Schema(description = "Ação executada", example = "CREATE", allowableValues = {"CREATE", "UPDATE", "DELETE", "SALE", "ENTRY", "LOSS"})
    private String action;

    @Schema(description = "Tipo de entidade afetada", example = "PRODUCT")
    private String entityType;

    @Schema(description = "ID da entidade afetada", example = "10")
    private Long entityId;

    @Schema(description = "ID do usuário que executou a ação", example = "1")
    private Long userId;

    @Schema(description = "Email do usuário que executou a ação", example = "admin@estoque.com")
    private String userEmail;

    @Schema(description = "Valor antigo (em JSON)", example = "{\"name\":\"Produto Antigo\"}")
    private String oldValue;

    @Schema(description = "Valor novo (em JSON)", example = "{\"name\":\"Produto Novo\"}")
    private String newValue;

    @Schema(description = "Detalhes adicionais da ação")
    private String details;

    @Schema(description = "Data e hora da ação", example = "2024-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}