package com.example.EstoqueFacil.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStopEvent implements Serializable {
    private Long produtoId;
    private String nomeProduto;
    private Long diasSemVenda;
    private Double ultimoPrecoVenda;
    private LocalDateTime dataEvento;
}