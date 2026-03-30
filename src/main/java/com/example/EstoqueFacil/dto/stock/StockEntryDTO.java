package com.example.EstoqueFacil.dto.stock;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockEntryDTO {

    @NotNull(message = "Produto é obrigatório")
    private Long productId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Max(value = 999999, message = "Quantidade muito alta")
    private Integer quantity;

    private LocalDate expirationDate;

    @NotNull(message = "Usuário é obrigatório")
    private Long userId;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observation;
}