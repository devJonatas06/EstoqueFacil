package com.example.EstoqueFacil.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent implements Serializable {
    private Long produtoId;
    private String nomeProduto;
    private Integer quantidadeAtual;
    private Integer quantidadeMinima;
    private LocalDateTime dataEvento;
}
