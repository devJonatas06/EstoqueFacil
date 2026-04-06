package com.example.EstoqueFacil.dto.stock;

import com.example.EstoqueFacil.entity.StockMovementType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para registrar saída de produtos do estoque")
public class StockExitDTO {

    @Schema(description = "ID do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Produto é obrigatório")
    private Long productId;

    @Schema(description = "Quantidade de produtos que estão saindo", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @Schema(description = "ID do usuário que está registrando a saída", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Usuário é obrigatório")
    private Long userId;

    @Schema(description = "Observação sobre a saída", example = "Venda para cliente João")
    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observation;

    @Schema(description = "Tipo de saída", example = "SALE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SALE", "LOSS"})
    @NotNull(message = "Tipo de saída é obrigatório")
    private StockMovementType type;
}