package com.example.EstoqueFacil.model.mapper;

import com.example.EstoqueFacil.model.dto.response.stock.StockMovementResponseDTO;
import com.example.EstoqueFacil.model.entity.StockMovement;
import com.example.EstoqueFacil.model.entity.StockMovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockMapper {

    public StockMovementResponseDTO toResponseDTO(StockMovement movement) {
        return StockMovementResponseDTO.builder()
                .id(movement.getId())
                .productId(movement.getBatch().getProduct().getId())
                .productName(movement.getBatch().getProduct().getName())
                .productBarcode(movement.getBatch().getProduct().getBarcode())
                .quantity(movement.getQuantity())
                .type(movement.getType())
                .unitPrice(movement.getType() == StockMovementType.SALE ?
                        movement.getBatch().getProduct().getSalePrice() :
                        movement.getBatch().getProduct().getCostPrice())
                .totalValue(BigDecimal.valueOf(movement.getQuantity())
                        .multiply(movement.getType() == StockMovementType.SALE ?
                                movement.getBatch().getProduct().getSalePrice() :
                                movement.getBatch().getProduct().getCostPrice()))
                .userName(movement.getUser().getName())
                .observation(movement.getObservation())
                .movementDate(movement.getMovementDate())
                .build();
    }
}