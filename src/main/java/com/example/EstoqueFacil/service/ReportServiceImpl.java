package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductSalesDTO;
import com.example.EstoqueFacil.entity.StockMovement;
import com.example.EstoqueFacil.entity.StockMovementType;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductBatchRepository productBatchRepository;


    @Override
    public List<ProductSalesDTO> getMostSoldProducts() {
        return stockMovementRepository.findMostSoldProducts();
    }


    @Override
    public List<ProductSalesDTO> getLeastSoldProducts() {
        List<ProductSalesDTO> list = stockMovementRepository.findMostSoldProducts();
        Collections.reverse(list);
        return list;
    }


    @Override
    public BigDecimal getEstimatedProfit(LocalDateTime start, LocalDateTime end) {

        List<StockMovement> sales = stockMovementRepository
                .findByTypeAndPeriod(
                        StockMovementType.SALE,
                        start,
                        end,
                        Pageable.unpaged()
                )
                .getContent();

        BigDecimal profit = BigDecimal.ZERO;

        for (StockMovement sm : sales) {

            BigDecimal salePrice = sm.getBatch().getProduct().getSalePrice();
            BigDecimal costPrice = sm.getBatch().getProduct().getCostPrice();

            BigDecimal unitProfit = salePrice.subtract(costPrice);

            BigDecimal total = unitProfit.multiply(BigDecimal.valueOf(sm.getQuantity()));

            profit = profit.add(total);
        }

        return profit;
    }

    // =========================
    // HISTÓRICO POR PERÍODO
    // =========================
    @Override
    public List<StockMovement> getMovementsByPeriod(LocalDateTime start, LocalDateTime end) {
        return stockMovementRepository
                .findByMovementDateBetween(start, end, Pageable.unpaged())
                .getContent();
    }
}