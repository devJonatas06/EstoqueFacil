package com.example.EstoqueFacil.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchExpiredEvent implements Serializable {
    private Long produtoId;
    private String nomeProduto;
    private Long loteId;
    private LocalDate dataValidade;
    private Integer quantidade;
    private LocalDateTime dataEvento;
}