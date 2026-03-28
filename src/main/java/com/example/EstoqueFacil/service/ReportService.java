package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductSalesDTO;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.entity.StockMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<ProductSalesDTO> getMostSoldProducts();

    List<ProductSalesDTO> getLeastSoldProducts();

    BigDecimal getEstimatedProfit(LocalDateTime start, LocalDateTime end);

    List<StockMovement> getMovementsByPeriod(LocalDateTime start, LocalDateTime end);
    // Adicionar no ReportService.java

    Map<String, BigDecimal> getProfitByProduct(LocalDateTime start, LocalDateTime end);

    List<StockMovement> getMovementsByProduct(Long productId, LocalDateTime start, LocalDateTime end);

    List<ProductBatch> getExpiringProductsReport(int days);

    List<Product> getInactiveProductsReport(int days);
}