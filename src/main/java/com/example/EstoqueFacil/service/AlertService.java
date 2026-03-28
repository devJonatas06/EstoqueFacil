package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;

import java.time.LocalDate;
import java.util.List;

public interface AlertService {

    // Produtos abaixo do estoque mínimo
    List<Product> getLowStockProducts();

    // Produtos parados há X dias (sem movimentação de venda)
    List<Product> getInactiveProducts(int days);

    // Lotes que vencem em X dias
    List<ProductBatch> getExpiringBatches(int days);

    // Lotes já vencidos
    List<ProductBatch> getExpiredBatches();

    // Produtos com estoque crítico (abaixo do mínimo há X dias)
    List<Product> getCriticalStockProducts(int days);

    // Resumo de alertas (para dashboard)
    AlertSummary getAlertSummary();
}