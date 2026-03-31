package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.entity.StockMovement;
import com.example.EstoqueFacil.entity.StockMovementType;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;

    @Override
    public List<BestSellingProductDTO> getBestSellingProducts() {
        List<Object[]> results = stockMovementRepository.findBestSellingProducts();
        return results.stream()
                .map(this::mapToBestSellingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BestSellingProductDTO> getWorstSellingProducts() {
        List<BestSellingProductDTO> best = getBestSellingProducts();
        Collections.reverse(best);
        return best;
    }

    @Override
    public ProfitReportDTO getProfitReport(LocalDateTime start, LocalDateTime end) {
        List<StockMovement> sales = stockMovementRepository
                .findByTypeAndPeriod(StockMovementType.SALE, start, end, Pageable.unpaged())
                .getContent();

        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalItems = 0;

        for (StockMovement sm : sales) {
            BigDecimal unitProfit = sm.getBatch().getProduct().getSalePrice()
                    .subtract(sm.getBatch().getProduct().getCostPrice());
            totalProfit = totalProfit.add(unitProfit.multiply(BigDecimal.valueOf(sm.getQuantity())));
            totalItems += sm.getQuantity();
        }

        BigDecimal averageTicket = totalItems > 0 ?
                totalProfit.divide(BigDecimal.valueOf(totalItems), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return ProfitReportDTO.builder()
                .totalProfit(totalProfit)
                .start(start)
                .end(end)
                .totalItemsSold(totalItems)
                .averageTicket(averageTicket)
                .build();
    }

    @Override
    public ProfitReportDTO getDetailedProfitReport(LocalDateTime start, LocalDateTime end) {
        ProfitReportDTO basic = getProfitReport(start, end);
        Map<String, BigDecimal> profitByProduct = getProfitByProduct(start, end);
        String topProduct = profitByProduct.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return ProfitReportDTO.builder()
                .totalProfit(basic.getTotalProfit())
                .start(start)
                .end(end)
                .profitByProduct(profitByProduct)
                .totalItemsSold(basic.getTotalItemsSold())
                .averageTicket(basic.getAverageTicket())
                .topProfitProduct(topProduct)
                .build();
    }

    @Override
    public List<InactiveProductDTO> getInactiveProducts(int days) {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(days);
        List<Product> products = productRepository.findProductsWithoutMovementSince(limitDate);

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

    @Override
    public List<ExpiringBatchDTO> getExpiringBatches(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<ProductBatch> batches = productBatchRepository.findExpiringBatchesBetween(today, endDate);

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

    @Override
    public List<StockMovementReportDTO> getMovementsByPeriod(LocalDateTime start, LocalDateTime end) {
        List<StockMovement> movements = stockMovementRepository
                .findByMovementDateBetween(start, end, Pageable.unpaged())
                .getContent();

        return movements.stream()
                .map(m -> StockMovementReportDTO.builder()
                        .movementId(m.getId())
                        .productId(m.getBatch().getProduct().getId())
                        .productName(m.getBatch().getProduct().getName())
                        .barcode(m.getBatch().getProduct().getBarcode())
                        .quantity(m.getQuantity())
                        .type(m.getType())
                        .unitPrice(m.getType() == StockMovementType.SALE ?
                                m.getBatch().getProduct().getSalePrice() :
                                m.getBatch().getProduct().getCostPrice())
                        .totalValue(BigDecimal.valueOf(m.getQuantity())
                                .multiply(m.getType() == StockMovementType.SALE ?
                                        m.getBatch().getProduct().getSalePrice() :
                                        m.getBatch().getProduct().getCostPrice()))
                        .userName(m.getUser().getName())
                        .observation(m.getObservation())
                        .movementDate(m.getMovementDate())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> getProfitByProduct(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = stockMovementRepository.findProfitByProduct(start, end);
        Map<String, BigDecimal> profitMap = new HashMap<>();

        for (Object[] result : results) {
            Long productId = (Long) result[0];
            BigDecimal profit = (BigDecimal) result[1];
            productRepository.findById(productId).ifPresent(product ->
                    profitMap.put(product.getName(), profit)
            );
        }
        return profitMap;
    }

    private BestSellingProductDTO mapToBestSellingDTO(Object[] result) {
        return BestSellingProductDTO.builder()
                .productId((Long) result[0])
                .productName((String) result[1])
                .barcode((String) result[2])
                .totalSold(((Number) result[3]).intValue())
                .totalRevenue((BigDecimal) result[4])
                .profit((BigDecimal) result[5])
                .build();
    }

    private Integer getCurrentStock(Long productId) {
        return productBatchRepository.getTotalStockByProduct(productId);
    }

    private LocalDateTime getLastMovementDate(Long productId) {
        return LocalDateTime.now().minusDays(10);
    }

    private Integer getDaysInactive(Long productId) {
        return 15;
    }

    private String getExpiringStatus(LocalDate expirationDate) {
        LocalDate today = LocalDate.now();
        long daysToExpire = today.until(expirationDate).getDays();

        if (daysToExpire < 7) return "URGENTE";
        if (daysToExpire < 30) return "ATENÇÃO";
        return "OK";
    }
}