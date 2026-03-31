package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.service.AlertService;
import com.example.EstoqueFacil.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
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
        AlertSummaryDTO response = alertService.getAlertSummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Detalhes completos dos alertas")
    public ResponseEntity<AlertDetailDTO> getAlertDetails() {
        AlertDetailDTO response = alertService.getAlertDetails();
        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS MAIS/MENOS VENDIDOS
    // =========================
    @GetMapping("/best-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos mais vendidos")
    public ResponseEntity<List<BestSellingProductDTO>> getBestSellers() {
        List<BestSellingProductDTO> response = reportService.getBestSellingProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/worst-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos menos vendidos (menor giro)")
    public ResponseEntity<List<BestSellingProductDTO>> getWorstSellers() {
        List<BestSellingProductDTO> response = reportService.getWorstSellingProducts();
        return ResponseEntity.ok(response);
    }

    // =========================
    // LUCRO
    // =========================
    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Lucro estimado por período")
    public ResponseEntity<ProfitReportDTO> getProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (includeDetails) {
            ProfitReportDTO response = reportService.getDetailedProfitReport(start, end);
            return ResponseEntity.ok(response);
        }
        ProfitReportDTO response = reportService.getProfitReport(start, end);
        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS PARADOS
    // =========================
    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos parados há X dias")
    public ResponseEntity<List<InactiveProductDTO>> getInactiveProducts(
            @RequestParam(defaultValue = "30") int days) {
        List<InactiveProductDTO> response = reportService.getInactiveProducts(days);
        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS COM ESTOQUE BAIXO
    // =========================
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Produtos com estoque abaixo do mínimo")
    public ResponseEntity<List<LowStockProductDTO>> getLowStockProducts() {
        List<LowStockProductDTO> response = alertService.getLowStockProductsDTO();
        return ResponseEntity.ok(response);
    }

    // =========================
    // PRODUTOS PRÓXIMOS AO VENCIMENTO
    // =========================
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Lotes próximos ao vencimento")
    public ResponseEntity<List<ExpiringBatchDTO>> getExpiringBatches(
            @RequestParam(defaultValue = "30") int days) {
        List<ExpiringBatchDTO> response = reportService.getExpiringBatches(days);
        return ResponseEntity.ok(response);
    }

    // =========================
    // HISTÓRICO POR PERÍODO
    // =========================
    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Histórico de movimentações por período")
    public ResponseEntity<List<StockMovementReportDTO>> getMovementsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<StockMovementReportDTO> response = reportService.getMovementsByPeriod(start, end);
        return ResponseEntity.ok(response);
    }
}