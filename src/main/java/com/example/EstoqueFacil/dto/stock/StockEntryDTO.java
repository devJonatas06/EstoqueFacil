package com.example.EstoqueFacil.dto.stock;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para registrar entrada de produtos no estoque")
public class StockEntryDTO {

    @Schema(description = "ID do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Produto é obrigatório")
    private Long productId;

    @Schema(description = "Quantidade de produtos que estão entrando", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Max(value = 999999, message = "Quantidade muito alta")
    private Integer quantity;

    @Schema(description = "Data de validade do lote", example = "2025-12-31")
    private LocalDate expirationDate;

    @Schema(description = "ID do usuário que está registrando a entrada", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Usuário é obrigatório")
    private Long userId;

    @Schema(description = "Observação sobre a entrada", example = "Compra NF 12345")
    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observation;
}