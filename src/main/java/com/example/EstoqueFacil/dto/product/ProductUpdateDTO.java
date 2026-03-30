package com.example.EstoqueFacil.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductUpdateDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @Size(max = 100, message = "Fabricante deve ter no máximo 100 caracteres")
    private String maker;

    @NotNull(message = "Preço de custo é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de custo deve ser maior que zero")
    private BigDecimal costPrice;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal salePrice;

    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer minimumStock;
}