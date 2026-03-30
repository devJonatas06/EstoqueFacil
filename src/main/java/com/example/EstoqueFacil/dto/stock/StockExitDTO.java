package com.example.EstoqueFacil.dto.stock;

import com.example.EstoqueFacil.entity.StockMovementType;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class StockExitDTO {

    @NotNull(message = "Produto é obrigatório")
    private Long productId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantity;

    @NotNull(message = "Usuário é obrigatório")
    private Long userId;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observation;

    @NotNull(message = "Tipo de saída é obrigatório")
    private StockMovementType type;
}