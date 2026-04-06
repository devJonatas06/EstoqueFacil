package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.AlertService;
import com.example.EstoqueFacil.service.ReportService;
import com.example.EstoqueFacil.service.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios gerenciais e alertas")
@SecurityRequirement(name = "bearer-auth")
public class ReportController {

    private final AlertService alertService;
    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    @GetMapping("/alerts/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Resumo de alertas",
            description = "Retorna um resumo com contadores de todos os alertas do sistema (estoque baixo, produtos parados, lotes vencendo, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso")
    })
    public ResponseEntity<AlertSummaryDTO> getAlertSummary() {
        long startTime = System.currentTimeMillis();
        AlertSummaryDTO response = alertService.getAlertSummary();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Resumo de alertas gerado. Baixo: {}, Inativo: {}, Vencendo: {}, Vencidos: {}, Crítico: {}, Tempo: {}ms",
                response.getLowStockCount(), response.getInactiveProductsCount(),
                response.getExpiringSoonCount(), response.getExpiredCount(),
                response.getCriticalStockCount(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Detalhes dos alertas",
            description = "Retorna listas completas com detalhes de todos os alertas (produtos com estoque baixo, lotes vencendo, etc.)"
    )
    public ResponseEntity<AlertDetailDTO> getAlertDetails() {
        long startTime = System.currentTimeMillis();
        AlertDetailDTO response = alertService.getAlertDetails();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Detalhes de alertas gerado. Tempo: {}ms", duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/best-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Produtos mais vendidos",
            description = "Retorna ranking dos produtos mais vendidos do período (último ano)."
    )
    public ResponseEntity<List<BestSellingProductDTO>> getBestSellers() {
        long startTime = System.currentTimeMillis();
        List<BestSellingProductDTO> response = reportService.getBestSellingProducts();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Produtos mais vendidos gerado. Total: {}, Tempo: {}ms", response.size(), duration);

        if (!response.isEmpty()) {
            BestSellingProductDTO top = response.get(0);
            log.debug("Relatório - Top produto: {} - {} unidades", top.getProductName(), top.getTotalSold());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/worst-sellers")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Produtos menos vendidos",
            description = "Retorna ranking dos produtos com menor giro de vendas."
    )
    public ResponseEntity<List<BestSellingProductDTO>> getWorstSellers() {
        long startTime = System.currentTimeMillis();
        List<BestSellingProductDTO> response = reportService.getWorstSellingProducts();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Produtos menos vendidos gerado. Total: {}, Tempo: {}ms", response.size(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Relatório de lucro por período",
            description = "Calcula o lucro total do período especificado.\n\n" +
                    "**Regras:**\n" +
                    "- Período máximo: 1 ano\n" +
                    "- Data de início deve ser anterior à data de fim\n" +
                    "- Use `includeDetails=true` para obter lucro por produto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Período inválido (início após fim ou período muito longo)", content = @Content)
    })
    public ResponseEntity<ProfitReportDTO> getProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Data de início deve ser passada ou presente") LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Data de fim deve ser passada ou presente") LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.info("Relatório - Lucro solicitado. Período: {} a {}, Detalhado: {}", start, end, includeDetails);

        if (start.isAfter(end)) {
            log.warn("Relatório - Período inválido: início após fim. Início: {}, Fim: {}", start, end);
            throw new BusinessException("Data de início deve ser anterior à data de fim");
        }

        if (start.plusYears(1).isBefore(end)) {
            log.warn("Relatório - Período muito longo (máx 1 ano). Início: {}, Fim: {}", start, end);
            throw new BusinessException("Período máximo permitido é de 1 ano");
        }

        long startTime = System.currentTimeMillis();
        ProfitReportDTO response = includeDetails ?
                reportService.getDetailedProfitReport(start, end) :
                reportService.getProfitReport(start, end);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Lucro gerado. Total: R$ {}, Items: {}, Tempo: {}ms",
                response.getTotalProfit(), response.getTotalItemsSold(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Produtos parados",
            description = "Lista produtos que não tiveram movimentação de venda nos últimos X dias (padrão: 30 dias, máximo: 365)."
    )
    public ResponseEntity<List<InactiveProductDTO>> getInactiveProducts(
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "Dias deve ser maior que 0") @Max(value = 365, message = "Dias não pode exceder 365") int days) {

        log.info("Relatório - Produtos parados há {} dias", days);

        long startTime = System.currentTimeMillis();
        List<InactiveProductDTO> response = reportService.getInactiveProducts(days);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Relatório - Produtos parados encontrados: {}, Tempo: {}ms", response.size(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Estoque baixo",
            description = "Lista produtos com estoque atual abaixo do estoque mínimo configurado."
    )
    public ResponseEntity<List<LowStockProductDTO>> getLowStockProducts() {
        long startTime = System.currentTimeMillis();
        List<LowStockProductDTO> response = alertService.getLowStockProductsDTO();
        long duration = System.currentTimeMillis() - startTime;

        long criticalCount = response.stream().filter(p -> "CRÍTICO".equals(p.getStatus())).count();
        log.info("Relatório - Estoque baixo. Total: {}, Crítico: {}, Tempo: {}ms", response.size(), criticalCount, duration);

        if (criticalCount > 0) {
            log.warn("Relatório - Existem {} produtos em situação CRÍTICA", criticalCount);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Lotes próximos ao vencimento",
            description = "Lista lotes que vencem nos próximos X dias (padrão: 30 dias, máximo: 180)."
    )
    public ResponseEntity<List<ExpiringBatchDTO>> getExpiringBatches(
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "Dias deve ser maior que 0") @Max(value = 180, message = "Dias não pode exceder 180") int days) {

        log.info("Relatório - Lotes próximos ao vencimento em {} dias", days);

        long startTime = System.currentTimeMillis();
        List<ExpiringBatchDTO> response = reportService.getExpiringBatches(days);
        long duration = System.currentTimeMillis() - startTime;

        long urgentCount = response.stream().filter(b -> "URGENTE".equals(b.getStatus())).count();
        log.info("Relatório - Lotes encontrados: {}, Urgentes: {}, Tempo: {}ms", response.size(), urgentCount, duration);

        if (urgentCount > 0) {
            log.warn("Relatório - {} lotes em situação URGENTE (vencimento < 7 dias)", urgentCount);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Histórico de movimentações por período",
            description = "Retorna todas as movimentações de estoque (entradas, vendas, perdas) no período especificado.\n\n" +
                    "**Regras:**\n" +
                    "- Período máximo: 90 dias\n" +
                    "- Data de início deve ser anterior à data de fim"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Período inválido (início após fim ou período muito longo)", content = @Content)
    })
    public ResponseEntity<List<StockMovementReportDTO>> getMovementsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Data de início deve ser passada ou presente") LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Data de fim deve ser passada ou presente") LocalDateTime end) {

        log.info("Relatório - Movimentações solicitadas. Período: {} a {}", start, end);

        if (start.isAfter(end)) {
            log.warn("Relatório - Período inválido: início após fim. Início: {}, Fim: {}", start, end);
            throw new BusinessException("Data de início deve ser anterior à data de fim");
        }

        if (start.plusDays(90).isBefore(end)) {
            log.warn("Relatório - Período muito longo (máx 90 dias). Início: {}, Fim: {}", start, end);
            throw new BusinessException("Período máximo permitido é de 90 dias");
        }

        long startTime = System.currentTimeMillis();
        List<StockMovementReportDTO> response = reportService.getMovementsByPeriod(start, end);
        long duration = System.currentTimeMillis() - startTime;

        long entries = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.ENTRY).count();
        long sales = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.SALE).count();
        long losses = response.stream().filter(m -> m.getType() == com.example.EstoqueFacil.entity.StockMovementType.LOSS).count();

        log.info("Relatório - Movimentações gerado. Registros: {}, Entradas: {}, Vendas: {}, Perdas: {}, Tempo: {}ms",
                response.size(), entries, sales, losses, duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Exportar relatórios em PDF",
            description = "Gera um arquivo PDF completo com todos os relatórios (financeiro, estoque, perdas, performance)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar PDF", content = @Content)
    })
    public ResponseEntity<byte[]> exportAllReportsToPdf() {
        log.info("Relatório - Solicitação de exportação PDF");

        long startTime = System.currentTimeMillis();

        try {
            byte[] pdfBytes = pdfReportService.generateCompleteReport();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Relatório - PDF gerado com sucesso. Tamanho: {} bytes, Tempo: {}ms", pdfBytes.length, duration);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=relatorio_completo_" +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Relatório - Erro ao gerar PDF: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao gerar relatório PDF: " + e.getMessage());
        }
    }
}