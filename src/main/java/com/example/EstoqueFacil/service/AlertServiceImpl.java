package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository stockMovementRepository;

    // =========================
    // MÉTODOS QUE RETORNAM ENTIDADES
    // =========================
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

    // =========================
    // MÉTODOS QUE RETORNAM DTOS (para API)
    // =========================
    @Override
    public AlertSummaryDTO getAlertSummary() {
        return AlertSummaryDTO.builder()
                .lowStockCount(getLowStockProducts().size())
                .inactiveProductsCount(getInactiveProducts(30).size())
                .expiringSoonCount(getExpiringBatches(30).size())
                .expiredCount(getExpiredBatches().size())
                .criticalStockCount(getCriticalStockProducts(7).size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public AlertDetailDTO getAlertDetails() {
        return AlertDetailDTO.builder()
                .lowStockProducts(convertToLowStockDTO(getLowStockProducts()))
                .inactiveProducts(convertToInactiveDTO(getInactiveProducts(30)))
                .expiringBatches(convertToExpiringDTO(getExpiringBatches(30)))
                .expiredBatches(convertToExpiredDTO(getExpiredBatches()))
                .criticalStockProducts(convertToCriticalDTO(getCriticalStockProducts(7)))
                .build();
    }

    // =========================
    // MÉTODOS DE CONVERSÃO PARA DTO
    // =========================
    private List<LowStockProductDTO> convertToLowStockDTO(List<Product> products) {
        return products.stream()
                .map(p -> LowStockProductDTO.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .barcode(p.getBarcode())
                        .currentStock(getCurrentStock(p.getId()))
                        .minimumStock(p.getMinimumStock())
                        .deficit(p.getMinimumStock() - getCurrentStock(p.getId()))
                        .status(getStockStatus(p))
                        .daysBelowMinimum(getDaysBelowMinimum(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<InactiveProductDTO> convertToInactiveDTO(List<Product> products) {
        return products.stream()
                .map(p -> InactiveProductDTO.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .barcode(p.getBarcode())
                        .currentStock(getCurrentStock(p.getId()))
                        .lastMovementDate(getLastMovementDate(p.getId()))
                        .daysInactive(getDaysInactive(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ExpiringBatchDTO> convertToExpiringDTO(List<ProductBatch> batches) {
        LocalDate today = LocalDate.now();
        return batches.stream()
                .map(b -> ExpiringBatchDTO.builder()
                        .batchId(b.getId())
                        .productId(b.getProduct().getId())
                        .productName(b.getProduct().getName())
                        .barcode(b.getProduct().getBarcode())
                        .quantity(b.getQuantity())
                        .expirationDate(b.getExpirationDate())
                        .daysToExpire((int) today.until(b.getExpirationDate()).getDays())
                        .status(getExpiringStatus(b.getExpirationDate()))
                        .costValue(BigDecimal.valueOf(b.getQuantity())
                                .multiply(b.getProduct().getCostPrice()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ExpiredBatchDTO> convertToExpiredDTO(List<ProductBatch> batches) {
        LocalDate today = LocalDate.now();
        return batches.stream()
                .map(b -> ExpiredBatchDTO.builder()
                        .batchId(b.getId())
                        .productId(b.getProduct().getId())
                        .productName(b.getProduct().getName())
                        .barcode(b.getProduct().getBarcode())
                        .quantity(b.getQuantity())
                        .expirationDate(b.getExpirationDate())
                        .daysExpired(today.compareTo(b.getExpirationDate()))
                        .estimatedLoss(BigDecimal.valueOf(b.getQuantity())
                                .multiply(b.getProduct().getCostPrice()))
                        .status("EXPIRED")
                        .build())
                .collect(Collectors.toList());
    }

    private List<CriticalStockProductDTO> convertToCriticalDTO(List<Product> products) {
        return products.stream()
                .map(p -> CriticalStockProductDTO.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .barcode(p.getBarcode())
                        .currentStock(getCurrentStock(p.getId()))
                        .minimumStock(p.getMinimumStock())
                        .deficit(p.getMinimumStock() - getCurrentStock(p.getId()))
                        .daysBelowMinimum(getDaysBelowMinimum(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }


    private Integer getCurrentStock(Long productId) {
        return productBatchRepository.getTotalStockByProduct(productId);
    }

    private Integer getDaysBelowMinimum(Long productId) {
        return 5;
    }

    private LocalDateTime getLastMovementDate(Long productId) {
        return LocalDateTime.now().minusDays(10); // Placeholder
    }

    private Integer getDaysInactive(Long productId) {
        return 15; // Placeholder
    }

    private String getStockStatus(Product product) {
        Integer currentStock = getCurrentStock(product.getId());
        Integer minimumStock = product.getMinimumStock();

        if (currentStock >= minimumStock) {
            return "OK";
        } else if (currentStock >= minimumStock / 2) {
            return "LOW";
        } else {
            return "CRITICAL";
        }
    }

    private String getExpiringStatus(LocalDate expirationDate) {
        LocalDate today = LocalDate.now();
        long daysToExpire = today.until(expirationDate).getDays();

        if (daysToExpire < 7) {
            return "URGENTE";
        } else if (daysToExpire < 30) {
            return "ATENÇÃO";
        } else {
            return "OK";
        }
    }
}