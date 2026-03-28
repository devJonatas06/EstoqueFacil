package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.StockMovementType;

import java.time.LocalDate;
import java.util.List;

public interface StockService {

    void registerEntry(Long productId, Integer quantity, LocalDate expirationDate, Long userId, String observation);

    void registerExit(Long productId, Integer quantity, Long userId, String observation, StockMovementType type);

    // =========================
    // ALERTAS
    // =========================
    List<Product> getLowStockProducts();
}