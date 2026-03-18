package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.StockMovement;
import com.example.EstoqueFacil.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByType(StockMovementType type);

    List<StockMovement> findByMovementDateBetween(LocalDateTime start, LocalDateTime end);

    List<StockMovement> findByUserId(Long userId);

    List<StockMovement> findByBatchProductId(Long productId);
}