package com.example.EstoqueFacil.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estrutura padronizada para respostas de erro")
public class ErrorResponseDTO {

    @Schema(description = "Data e hora do erro", example = "2024-01-15 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP do erro", example = "400")
    private Integer status;

    @Schema(description = "Descrição resumida do erro", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem detalhada do erro", example = "Nome do produto é obrigatório")
    private String message;

    @Schema(description = "Endpoint que gerou o erro", example = "/api/v1/products")
    private String path;

    @Schema(description = "Erros de validação por campo (para requisições inválidas)")
    private Map<String, String> validationErrors;
}