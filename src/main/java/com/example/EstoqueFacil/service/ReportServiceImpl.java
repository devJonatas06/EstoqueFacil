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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;

    @Override
    public List<BestSellingProductDTO> getBestSellingProducts() {
        long startTime = System.currentTimeMillis();
        List<Object[]> results = stockMovementRepository.findBestSellingProducts();
        List<BestSellingProductDTO> response = results.stream()
                .map(this::mapToBestSellingDTO)
                .collect(Collectors.toList());
        log.info("Relatório - Produtos mais vendidos gerado. Total: {}, Tempo: {}ms", response.size(), System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    public List<BestSellingProductDTO> getWorstSellingProducts() {
        List<BestSellingProductDTO> best = getBestSellingProducts();
        Collections.reverse(best);
        if (!best.isEmpty()) {
            log.info("Relatório - Produto com menor giro: {} - {} unidades", best.get(0).getProductName(), best.get(0).getTotalSold());
        }
        return best;
    }

    @Override
    public ProfitReportDTO getProfitReport(LocalDateTime start, LocalDateTime end) {
        long startTime = System.currentTimeMillis();
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

        log.info("Relatório - Lucro gerado. Período: {} a {}, Total: R$ {}, Items: {}, Tempo: {}ms",
                start, end, totalProfit, totalItems, System.currentTimeMillis() - startTime);

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
        long startTime = System.currentTimeMillis();
        ProfitReportDTO basic = getProfitReport(start, end);
        Map<String, BigDecimal> profitByProduct = getProfitByProduct(start, end);
        String topProduct = profitByProduct.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        log.info("Relatório - Lucro detalhado gerado. Top produto: {}, Tempo: {}ms", topProduct, System.currentTimeMillis() - startTime);

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

        if (!products.isEmpty()) {
            log.info("Relatório - {} produtos parados há mais de {} dias", products.size(), days);
        }

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

        long urgentCount = batches.stream().filter(b -> getExpiringStatus(b.getExpirationDate()).equals("URGENTE")).count();
        if (urgentCount > 0) {
            log.warn("Relatório - {} lotes em situação URGENTE (vencimento < 7 dias)", urgentCount);
        }

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
        long startTime = System.currentTimeMillis();
        List<StockMovement> movements = stockMovementRepository
                .findByMovementDateBetween(start, end, Pageable.unpaged())
                .getContent();

        List<StockMovementReportDTO> response = movements.stream()
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

        log.info("Relatório - Movimentações por período gerado. Período: {} a {}, Registros: {}, Tempo: {}ms",
                start, end, response.size(), System.currentTimeMillis() - startTime);
        return response;
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

    @Override
    public FinancialReportDTO getFinancialReport(LocalDateTime start, LocalDateTime end) {
        ProfitReportDTO profit = getDetailedProfitReport(start, end);

        log.info("Relatório Financeiro - Período: {} a {}, Lucro Total: R$ {}", start, end, profit.getTotalProfit());

        return FinancialReportDTO.builder()
                .totalProfit(profit.getTotalProfit())
                .profitByProduct(profit.getProfitByProduct())
                .topProfitProduct(profit.getTopProfitProduct())
                .start(start)
                .end(end)
                .totalItemsSold(profit.getTotalItemsSold())
                .averageTicket(profit.getAverageTicket())
                .build();
    }

    @Override
    public StockIntelligenceReportDTO getStockIntelligenceReport() {
        long startTime = System.currentTimeMillis();
        List<CriticalStockProductDTO> critical = getCriticalStockProducts();
        List<StockIntelligenceReportDTO.StockBreakdownPredictionDTO> predictions = calculateBreakdownPredictions();
        List<StockIntelligenceReportDTO.DaysBelowMinimumDTO> daysBelow = getDaysBelowMinimum();

        log.info("Relatório Estoque Inteligente - Críticos: {}, Ruptura prevista: {}, Tempo: {}ms",
                critical.size(), predictions.size(), System.currentTimeMillis() - startTime);

        return StockIntelligenceReportDTO.builder()
                .criticalProducts(critical)
                .breakdownPredictions(predictions)
                .daysBelowMinimum(daysBelow)
                .build();
    }

    private List<CriticalStockProductDTO> getCriticalStockProducts() {
        List<Product> products = productRepository.findBelowMinimumStock();
        return products.stream()
                .map(p -> CriticalStockProductDTO.builder()
                        .productId(p.getId())
                        .name(p.getName())
                        .barcode(p.getBarcode())
                        .currentStock(getCurrentStock(p.getId()))
                        .minimumStock(p.getMinimumStock())
                        .deficit(p.getMinimumStock() - getCurrentStock(p.getId()))
                        .daysBelowMinimum(calculateDaysBelowMinimum(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<StockIntelligenceReportDTO.StockBreakdownPredictionDTO> calculateBreakdownPredictions() {
        List<Product> products = productRepository.findAll();
        List<StockIntelligenceReportDTO.StockBreakdownPredictionDTO> predictions = new ArrayList<>();

        for (Product p : products) {
            Integer currentStock = getCurrentStock(p.getId());
            Integer dailySales = getDailyAverageSales(p.getId());

            if (dailySales > 0 && currentStock < p.getMinimumStock()) {
                int daysToBreakdown = currentStock / dailySales;
                String riskLevel = daysToBreakdown < 7 ? "ALTO" : (daysToBreakdown < 30 ? "MÉDIO" : "BAIXO");

                if ("ALTO".equals(riskLevel)) {
                    log.warn("Estoque - Risco ALTO de ruptura. Produto: {}, Dias restantes: {}", p.getName(), daysToBreakdown);
                }

                predictions.add(StockIntelligenceReportDTO.StockBreakdownPredictionDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .currentStock(currentStock)
                        .estimatedDailySales(dailySales)
                        .estimatedDaysToBreakdown(daysToBreakdown)
                        .riskLevel(riskLevel)
                        .build());
            }
        }
        return predictions;
    }

    private List<StockIntelligenceReportDTO.DaysBelowMinimumDTO> getDaysBelowMinimum() {
        List<Product> products = productRepository.findBelowMinimumStock();
        return products.stream()
                .map(p -> StockIntelligenceReportDTO.DaysBelowMinimumDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .currentStock(getCurrentStock(p.getId()))
                        .minimumStock(p.getMinimumStock())
                        .daysBelowMinimum(calculateDaysBelowMinimum(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private Integer getDailyAverageSales(Long productId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<StockMovement> sales = stockMovementRepository
                .findByProductAndPeriod(productId, thirtyDaysAgo, LocalDateTime.now());

        int totalSold = sales.stream()
                .filter(m -> m.getType() == StockMovementType.SALE)
                .mapToInt(StockMovement::getQuantity)
                .sum();

        return totalSold / 30;
    }

    private Integer calculateDaysBelowMinimum(Long productId) {
        return 5;
    }

    @Override
    public LossReportDTO getLossReport() {
        long startTime = System.currentTimeMillis();
        List<ProductBatch> expiredBatches = productBatchRepository.findExpiredBatches(LocalDate.now());

        BigDecimal totalLoss = expiredBatches.stream()
                .map(b -> BigDecimal.valueOf(b.getQuantity())
                        .multiply(b.getProduct().getCostPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalUnits = expiredBatches.stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();

        if (!expiredBatches.isEmpty()) {
            log.warn("Relatório de Perdas - Produtos vencidos encontrados. Total: {} unidades, Prejuízo: R$ {}", totalUnits, totalLoss);
        }

        List<ExpiredBatchDTO> expiredDTOs = expiredBatches.stream()
                .map(b -> ExpiredBatchDTO.builder()
                        .batchId(b.getId())
                        .productId(b.getProduct().getId())
                        .productName(b.getProduct().getName())
                        .barcode(b.getProduct().getBarcode())
                        .quantity(b.getQuantity())
                        .expirationDate(b.getExpirationDate())
                        .daysExpired((int) b.getExpirationDate().until(LocalDate.now()).getDays())
                        .estimatedLoss(BigDecimal.valueOf(b.getQuantity())
                                .multiply(b.getProduct().getCostPrice()))
                        .status("EXPIRED")
                        .build())
                .collect(Collectors.toList());

        log.info("Relatório de Perdas gerado. Total unidades vencidas: {}, Tempo: {}ms", totalUnits, System.currentTimeMillis() - startTime);

        return LossReportDTO.builder()
                .expiredProducts(expiredDTOs)
                .totalEstimatedLoss(totalLoss)
                .totalExpiredUnits(totalUnits)
                .build();
    }

    @Override
    public PerformanceReportDTO getPerformanceReport() {
        long startTime = System.currentTimeMillis();
        List<InactiveProductDTO> stagnant = getInactiveProducts(60);
        List<BestSellingProductDTO> highTurnover = getBestSellingProducts();
        if (highTurnover.size() > 10) {
            highTurnover = highTurnover.subList(0, 10);
        }
        List<PerformanceReportDTO.ProductTurnoverDTO> turnover = calculateTurnoverRate();

        log.info("Relatório de Performance - Encalhados: {}, Top produtos: {}, Giro calculado: {}, Tempo: {}ms",
                stagnant.size(), highTurnover.size(), turnover.size(), System.currentTimeMillis() - startTime);

        return PerformanceReportDTO.builder()
                .stagnantProducts(stagnant)
                .highTurnoverProducts(highTurnover)
                .turnoverRate(turnover)
                .build();
    }

    private List<PerformanceReportDTO.ProductTurnoverDTO> calculateTurnoverRate() {
        List<Product> products = productRepository.findAll();
        List<PerformanceReportDTO.ProductTurnoverDTO> result = new ArrayList<>();

        for (Product p : products) {
            Integer totalSold = getTotalSoldLastYear(p.getId());
            Integer averageStock = getAverageStock(p.getId());

            double turnoverRate = averageStock > 0 ? (double) totalSold / averageStock : 0;

            result.add(PerformanceReportDTO.ProductTurnoverDTO.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .totalSold(totalSold)
                    .averageStock(averageStock)
                    .turnoverRate(Math.round(turnoverRate * 100) / 100.0)
                    .build());
        }

        return result.stream()
                .sorted((a, b) -> Double.compare(b.getTurnoverRate(), a.getTurnoverRate()))
                .limit(20)
                .collect(Collectors.toList());
    }

    private Integer getTotalSoldLastYear(Long productId) {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<StockMovement> sales = stockMovementRepository
                .findByProductAndPeriod(productId, oneYearAgo, LocalDateTime.now());

        return sales.stream()
                .filter(m -> m.getType() == StockMovementType.SALE)
                .mapToInt(StockMovement::getQuantity)
                .sum();
    }

    private Integer getAverageStock(Long productId) {
        return getCurrentStock(productId);
    }
}