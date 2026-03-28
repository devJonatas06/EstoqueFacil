package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AlertSummary;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findBelowMinimumStock();
    }

    @Override
    public List<Product> getInactiveProducts(int days) {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(days);
        return productRepository.findProductsWithoutMovementSince(limitDate);
    }

    @Override
    public List<ProductBatch> getExpiringBatches(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return productBatchRepository.findExpiringBatchesBetween(today, endDate);
    }

    @Override
    public List<ProductBatch> getExpiredBatches() {
        return productBatchRepository.findExpiredBatches(LocalDate.now());
    }

    @Override
    public List<Product> getCriticalStockProducts(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return productRepository.findCriticalStockSince(since);
    }

    @Override
    public AlertSummary getAlertSummary() {
        List<Product> lowStock = getLowStockProducts();
        List<Product> inactive = getInactiveProducts(30);
        List<ProductBatch> expiring = getExpiringBatches(30);
        List<ProductBatch> expired = getExpiredBatches();
        List<Product> critical = getCriticalStockProducts(7);

        return AlertSummary.builder()
                .lowStockCount(lowStock.size())
                .inactiveProductsCount(inactive.size())
                .expiringBatchesCount(expiring.size())
                .expiredBatchesCount(expired.size())
                .criticalStockCount(critical.size())
                .lowStockProducts(lowStock)
                .inactiveProducts(inactive)
                .expiringBatches(expiring)
                .expiredBatches(expired)
                .criticalStockProducts(critical)
                .build();
    }
}