package com.example.EstoqueFacil.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados para criação de um novo produto")
public class ProductRequestDTO {

    @Schema(description = "Nome do produto", example = "Smartphone Galaxy S23", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Schema(description = "Código de barras do produto (formato EAN-13)", example = "7891234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Código de barras é obrigatório")
    @Pattern(regexp = "^[0-9]{8,14}$", message = "Código de barras inválido (8-14 dígitos)")
    private String barcode;

    @Schema(description = "Descrição detalhada do produto", example = "Smartphone 256GB, 8GB RAM, Tela 6.7 polegadas")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @Schema(description = "Fabricante/Marca do produto", example = "Samsung")
    @Size(max = 100, message = "Fabricante deve ter no máximo 100 caracteres")
    private String maker;

    @Schema(description = "Preço de custo do produto", example = "2500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Preço de custo é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de custo deve ser maior que zero")
    private BigDecimal costPrice;

    @Schema(description = "Preço de venda do produto", example = "3500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal salePrice;

    @Schema(description = "Estoque mínimo para alerta", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer minimumStock;

    @Schema(description = "ID da categoria do produto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Categoria é obrigatória")
    private Long categoryId;
}