package com.example.EstoqueFacil.mapper;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.entity.StockMovement;
import com.example.EstoqueFacil.entity.StockMovementType;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public StockEntryDTO toEntryDTO(Long productId, Integer quantity, 
                                     java.time.LocalDate expirationDate, 
                                     Long userId, String observation) {
        return StockEntryDTO.builder()
                .productId(productId)
                .quantity(quantity)
                .expirationDate(expirationDate)
                .userId(userId)
                .observation(observation)
                .build();
    }

    public StockExitDTO toExitDTO(Long productId, Integer quantity, 
                                   Long userId, String observation, 
                                   StockMovementType type) {
        return StockExitDTO.builder()
                .productId(productId)
                .quantity(quantity)
                .userId(userId)
                .observation(observation)
                .type(type)
                .build();
    }
}