package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.AlertService;
import com.example.EstoqueFacil.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Endpoints para relatórios e alertas")
@SecurityRequirement(name = "bearer-auth")
public class ReportController {

    private final AlertService alertService;
    private final ReportService reportService;

    // =========================
    // ALERTAS
    // =========================

    @GetMapping("/alerts/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Resumo de alertas (apenas contadores)")
    public ResponseEntity<AlertSummaryDTO> getAlertSummary() {
        log.info("Requisição recebida: resumo de alertas");

        long startTime = System.currentTimeMillis();
        AlertSummaryDTO response = alertService.getAlertSummary();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Resumo de alertas gerado em {}ms. Contadores: lowStock={}, inactive={}, expiring={}, expired={}, critical={}",
                duration,
                response.getLowStockCount(),
                response.getInactiveProductsCount(),
                response.getExpiringSoonCount(),
                response.getExpiredCount(),
                response.getCriticalStockCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Detalhes completos dos alertas")
    public ResponseEntity<AlertDetailDTO> getAlertDetails() {
        log.info("Requisição recebida: detalhes completos dos alertas");

        long startTime = System.currentTimeMillis();
        AlertDetailDTO response = alertService.getAlertDetails();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Detalhes de alertas gerados em {}ms. lowStock={}, inactive={}, expiring={}, expired={}, critical={}",
                duration,
                response.getLowStockProducts() != null ? response.getLowStockProducts().size() : 0,
                response.getInactiveProducts() != null ? response.getInactiveProducts().size() : 0,
                response.getExpiringBatches() != null ? response.getExpiringBatches().size() : 0,
                response.getExpiredBatches() != null ? response.getExpiredBatches().size() : 0,
                response.getCriticalStockProducts() != null ? response.getCriticalStockProducts().size() : 0);

        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS MAIS/MENOS VENDIDOS
    // =========================

    @GetMapping("/best-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos mais vendidos")
    public ResponseEntity<List<BestSellingProductDTO>> getBestSellers() {
        log.info("Requisição recebida: produtos mais vendidos");

        long startTime = System.currentTimeMillis();
        List<BestSellingProductDTO> response = reportService.getBestSellingProducts();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório de mais vendidos gerado em {}ms. Total de produtos: {}", duration, response.size());

        if (!response.isEmpty()) {
            BestSellingProductDTO top = response.get(0);
            log.debug("Produto mais vendido: {} - {} unidades vendidas", top.getProductName(), top.getTotalSold());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/worst-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos menos vendidos (menor giro)")
    public ResponseEntity<List<BestSellingProductDTO>> getWorstSellers() {
        log.info("Requisição recebida: produtos menos vendidos");

        long startTime = System.currentTimeMillis();
        List<BestSellingProductDTO> response = reportService.getWorstSellingProducts();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório de menos vendidos gerado em {}ms. Total de produtos: {}", duration, response.size());

        return ResponseEntity.ok(response);
    }

    // =========================
    // LUCRO
    // =========================

    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Lucro estimado por período")
    public ResponseEntity<ProfitReportDTO> getProfitReport(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent(message = "Data de início deve ser passada ou presente")
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent(message = "Data de fim deve ser passada ou presente")
            LocalDateTime end,

            @RequestParam(defaultValue = "false")
            boolean includeDetails) {

        log.info("Requisição recebida: relatório de lucro. start={}, end={}, includeDetails={}", start, end, includeDetails);

        // ✅ VALIDAÇÃO: start deve ser antes de end
        if (start.isAfter(end)) {
            log.warn("Data de início posterior à data de fim. start={}, end={}", start, end);
            throw new BusinessException("Data de início deve ser anterior à data de fim");
        }

        // ✅ VALIDAÇÃO: período máximo de 1 ano (evita consultas muito pesadas)
        if (start.plusYears(1).isBefore(end)) {
            log.warn("Período muito longo solicitado: {} a {}", start, end);
            throw new BusinessException("Período máximo permitido é de 1 ano");
        }

        long startTime = System.currentTimeMillis();
        ProfitReportDTO response;

        if (includeDetails) {
            response = reportService.getDetailedProfitReport(start, end);
            log.info("Relatório de lucro detalhado gerado em {}ms. Lucro total: R$ {}, Items vendidos: {}, Ticket médio: R$ {}",
                    System.currentTimeMillis() - startTime,
                    response.getTotalProfit(),
                    response.getTotalItemsSold(),
                    response.getAverageTicket());

            if (response.getTopProfitProduct() != null) {
                log.debug("Produto que mais lucrou: {}", response.getTopProfitProduct());
            }
        } else {
            response = reportService.getProfitReport(start, end);
            log.info("Relatório de lucro resumido gerado em {}ms. Lucro total: R$ {}, Items vendidos: {}",
                    System.currentTimeMillis() - startTime,
                    response.getTotalProfit(),
                    response.getTotalItemsSold());
        }

        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS PARADOS
    // =========================

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos parados há X dias")
    public ResponseEntity<List<InactiveProductDTO>> getInactiveProducts(
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "Dias deve ser maior que 0")
            @Max(value = 365, message = "Dias não pode exceder 365 (1 ano)")
            int days) {

        log.info("Requisição recebida: produtos parados há {} dias", days);

        long startTime = System.currentTimeMillis();
        List<InactiveProductDTO> response = reportService.getInactiveProducts(days);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório de produtos parados gerado em {}ms. Produtos encontrados: {}", duration, response.size());

        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS COM ESTOQUE BAIXO
    // =========================

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos com estoque abaixo do mínimo")
    public ResponseEntity<List<LowStockProductDTO>> getLowStockProducts() {
        log.info("Requisição recebida: produtos com estoque baixo");

        long startTime = System.currentTimeMillis();
        List<LowStockProductDTO> response = alertService.getLowStockProductsDTO();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório de estoque baixo gerado em {}ms. Produtos encontrados: {}", duration, response.size());

        // ✅ Log dos produtos críticos
        long criticalCount = response.stream()
                .filter(p -> "CRÍTICO".equals(p.getStatus()))
                .count();

        if (criticalCount > 0) {
            log.warn("Produtos em situação CRÍTICA: {}", criticalCount);
        }

        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS PRÓXIMOS AO VENCIMENTO
    // =========================

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Lotes próximos ao vencimento")
    public ResponseEntity<List<ExpiringBatchDTO>> getExpiringBatches(
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "Dias deve ser maior que 0")
            @Max(value = 180, message = "Dias não pode exceder 180 (6 meses)")
            int days) {

        log.info("Requisição recebida: lotes próximos ao vencimento em {} dias", days);

        long startTime = System.currentTimeMillis();
        List<ExpiringBatchDTO> response = reportService.getExpiringBatches(days);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório de lotes expirando gerado em {}ms. Lotes encontrados: {}", duration, response.size());

        // ✅ Log dos lotes urgentes (< 7 dias)
        long urgentCount = response.stream()
                .filter(b -> "URGENTE".equals(b.getStatus()))
                .count();

        if (urgentCount > 0) {
            log.warn("Lotes em situação URGENTE (vencimento < 7 dias): {}", urgentCount);
        }

        return ResponseEntity.ok(response);
    }

    // =========================
    // HISTÓRICO POR PERÍODO
    // =========================

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Histórico de movimentações por período")
    public ResponseEntity<List<StockMovementReportDTO>> getMovementsByPeriod(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent(message = "Data de início deve ser passada ou presente")
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @PastOrPresent(message = "Data de fim deve ser passada ou presente")
            LocalDateTime end) {

        log.info("Requisição recebida: histórico de movimentações. start={}, end={}", start, end);

        // ✅ VALIDAÇÃO: start deve ser antes de end
        if (start.isAfter(end)) {
            log.warn("Data de início posterior à data de fim. start={}, end={}", start, end);
            throw new BusinessException("Data de início deve ser anterior à data de fim");
        }

        // ✅ VALIDAÇÃO: período máximo de 90 dias (evita consultas pesadas)
        if (start.plusDays(90).isBefore(end)) {
            log.warn("Período muito longo solicitado: {} a {} (máximo 90 dias)", start, end);
            throw new BusinessException("Período máximo permitido é de 90 dias para histórico de movimentações");
        }

        long startTime = System.currentTimeMillis();
        List<StockMovementReportDTO> response = reportService.getMovementsByPeriod(start, end);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Histórico de movimentações gerado em {}ms. Registros encontrados: {}", duration, response.size());

        // ✅ Log de entradas e saídas
        long entries = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.ENTRY).count();
        long sales = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.SALE).count();
        long losses = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.LOSS).count();

        log.info("Resumo do período: ENTRADAS={}, VENDAS={}, PERDAS={}", entries, sales, losses);

        return ResponseEntity.ok(response);
    }
}