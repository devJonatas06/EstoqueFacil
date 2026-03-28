package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductSalesDTO;
import com.example.EstoqueFacil.entity.StockMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    List<ProductSalesDTO> getMostSoldProducts();

    List<ProductSalesDTO> getLeastSoldProducts();

    BigDecimal getEstimatedProfit(LocalDateTime start, LocalDateTime end);

    List<StockMovement> getMovementsByPeriod(LocalDateTime start, LocalDateTime end);
}