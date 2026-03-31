package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.*;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    // Produtos mais/menos vendidos
    List<BestSellingProductDTO> getBestSellingProducts();
    List<BestSellingProductDTO> getWorstSellingProducts();

    // Lucro
    ProfitReportDTO getProfitReport(LocalDateTime start, LocalDateTime end);
    ProfitReportDTO getDetailedProfitReport(LocalDateTime start, LocalDateTime end);

    // Produtos parados
    List<InactiveProductDTO> getInactiveProducts(int days);

    // Lotes próximos ao vencimento
    List<ExpiringBatchDTO> getExpiringBatches(int days);

    // Histórico por período
    List<StockMovementReportDTO> getMovementsByPeriod(LocalDateTime start, LocalDateTime end);
}