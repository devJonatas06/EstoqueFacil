package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.*;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final ReportService reportService;

    public byte[] generateCompleteReport() throws Exception {
        log.info("PDF - Iniciando geração de relatório completo");

        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDoc);

        PdfFont font = PdfFontFactory.createFont();

        Paragraph title = new Paragraph("RELATÓRIO GERENCIAL COMPLETO")
                .setFont(font).setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
        document.add(title);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph date = new Paragraph("Gerado em: " + dateStr)
                .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(30);
        document.add(date);

        document.add(new Paragraph("RELATÓRIO FINANCEIRO")
                .setFont(font).setFontSize(14).setBold().setMarginTop(20));

        FinancialReportDTO financial = reportService.getFinancialReport(
                LocalDateTime.now().minusYears(1), LocalDateTime.now());

        log.info("PDF - Relatório financeiro gerado. Lucro total: R$ {}", financial.getTotalProfit());

        Table financialTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);

        financialTable.addCell(new Cell().add(new Paragraph("Lucro Total:").setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(financial.getTotalProfit())).setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph("Total de Itens Vendidos:").setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph(String.valueOf(financial.getTotalItemsSold())).setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph("Ticket Médio:").setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(financial.getAverageTicket())).setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph("Produto Mais Lucrativo:").setFont(font)));
        financialTable.addCell(new Cell().add(new Paragraph(financial.getTopProfitProduct() != null ? financial.getTopProfitProduct() : "N/A").setFont(font)));

        document.add(financialTable);

        if (financial.getProfitByProduct() != null && !financial.getProfitByProduct().isEmpty()) {
            document.add(new Paragraph("Lucro por Produto:").setFont(font).setFontSize(12).setBold().setMarginTop(10));
            Table profitTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).setWidth(UnitValue.createPercentValue(100));
            profitTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            profitTable.addCell(new Cell().add(new Paragraph("Lucro").setFont(font).setBold()));

            financial.getProfitByProduct().entrySet().stream().limit(15).forEach(entry -> {
                profitTable.addCell(new Cell().add(new Paragraph(entry.getKey()).setFont(font)));
                profitTable.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(entry.getValue())).setFont(font)));
            });
            document.add(profitTable);
        }

        document.add(new Paragraph("RELATÓRIO DE ESTOQUE INTELIGENTE").setFont(font).setFontSize(14).setBold().setMarginTop(20));

        StockIntelligenceReportDTO stockIntelligence = reportService.getStockIntelligenceReport();

        if (stockIntelligence.getCriticalProducts() != null && !stockIntelligence.getCriticalProducts().isEmpty()) {
            document.add(new Paragraph("Produtos Críticos (Abaixo do Mínimo):").setFont(font).setFontSize(12).setBold());
            Table criticalTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20})).setWidth(UnitValue.createPercentValue(100));
            criticalTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            criticalTable.addCell(new Cell().add(new Paragraph("Estoque").setFont(font).setBold()));
            criticalTable.addCell(new Cell().add(new Paragraph("Mínimo").setFont(font).setBold()));
            criticalTable.addCell(new Cell().add(new Paragraph("Déficit").setFont(font).setBold()));

            stockIntelligence.getCriticalProducts().forEach(p -> {
                criticalTable.addCell(new Cell().add(new Paragraph(p.getName()).setFont(font)));
                criticalTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCurrentStock())).setFont(font)));
                criticalTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getMinimumStock())).setFont(font)));
                criticalTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getDeficit())).setFont(font)));
            });
            document.add(criticalTable);
        }

        if (stockIntelligence.getBreakdownPredictions() != null && !stockIntelligence.getBreakdownPredictions().isEmpty()) {
            document.add(new Paragraph("Previsão de Ruptura:").setFont(font).setFontSize(12).setBold().setMarginTop(10));
            Table ruptureTable = new Table(UnitValue.createPercentArray(new float[]{40, 15, 15, 15, 15})).setWidth(UnitValue.createPercentValue(100));
            ruptureTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            ruptureTable.addCell(new Cell().add(new Paragraph("Estoque").setFont(font).setBold()));
            ruptureTable.addCell(new Cell().add(new Paragraph("Vendas/Dia").setFont(font).setBold()));
            ruptureTable.addCell(new Cell().add(new Paragraph("Dias p/ Ruptura").setFont(font).setBold()));
            ruptureTable.addCell(new Cell().add(new Paragraph("Risco").setFont(font).setBold()));

            stockIntelligence.getBreakdownPredictions().forEach(p -> {
                ruptureTable.addCell(new Cell().add(new Paragraph(p.getProductName()).setFont(font)));
                ruptureTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCurrentStock())).setFont(font)));
                ruptureTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getEstimatedDailySales())).setFont(font)));
                ruptureTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getEstimatedDaysToBreakdown())).setFont(font)));
                ruptureTable.addCell(new Cell().add(new Paragraph(p.getRiskLevel()).setFont(font)));
            });
            document.add(ruptureTable);
        }

        document.add(new Paragraph("RELATÓRIO DE PERDAS").setFont(font).setFontSize(14).setBold().setMarginTop(20));

        LossReportDTO loss = reportService.getLossReport();
        log.info("PDF - Relatório de perdas gerado. Prejuízo total: R$ {}", loss.getTotalEstimatedLoss());

        Table lossSummary = new Table(UnitValue.createPercentArray(new float[]{50, 50})).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
        lossSummary.addCell(new Cell().add(new Paragraph("Total de Produtos Vencidos:").setFont(font)));
        lossSummary.addCell(new Cell().add(new Paragraph(String.valueOf(loss.getTotalExpiredUnits())).setFont(font)));
        lossSummary.addCell(new Cell().add(new Paragraph("Prejuízo Estimado:").setFont(font)));
        lossSummary.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(loss.getTotalEstimatedLoss())).setFont(font)));
        document.add(lossSummary);

        if (loss.getExpiredProducts() != null && !loss.getExpiredProducts().isEmpty()) {
            document.add(new Paragraph("Produtos Vencidos:").setFont(font).setFontSize(12).setBold());
            Table expiredTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20})).setWidth(UnitValue.createPercentValue(100));
            expiredTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            expiredTable.addCell(new Cell().add(new Paragraph("Quantidade").setFont(font).setBold()));
            expiredTable.addCell(new Cell().add(new Paragraph("Vencimento").setFont(font).setBold()));
            expiredTable.addCell(new Cell().add(new Paragraph("Prejuízo").setFont(font).setBold()));

            loss.getExpiredProducts().forEach(p -> {
                expiredTable.addCell(new Cell().add(new Paragraph(p.getProductName()).setFont(font)));
                expiredTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getQuantity())).setFont(font)));
                expiredTable.addCell(new Cell().add(new Paragraph(p.getExpirationDate().toString()).setFont(font)));
                expiredTable.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(p.getEstimatedLoss())).setFont(font)));
            });
            document.add(expiredTable);
        }

        document.add(new Paragraph("RELATÓRIO DE PERFORMANCE").setFont(font).setFontSize(14).setBold().setMarginTop(20));

        PerformanceReportDTO performance = reportService.getPerformanceReport();

        if (performance.getHighTurnoverProducts() != null && !performance.getHighTurnoverProducts().isEmpty()) {
            document.add(new Paragraph("Produtos Mais Vendidos (Top 10):").setFont(font).setFontSize(12).setBold());
            Table highTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25})).setWidth(UnitValue.createPercentValue(100));
            highTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            highTable.addCell(new Cell().add(new Paragraph("Unidades Vendidas").setFont(font).setBold()));
            highTable.addCell(new Cell().add(new Paragraph("Faturamento").setFont(font).setBold()));

            performance.getHighTurnoverProducts().forEach(p -> {
                highTable.addCell(new Cell().add(new Paragraph(p.getProductName()).setFont(font)));
                highTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getTotalSold())).setFont(font)));
                highTable.addCell(new Cell().add(new Paragraph("R$ " + formatMoney(p.getTotalRevenue())).setFont(font)));
            });
            document.add(highTable);
        }

        if (performance.getStagnantProducts() != null && !performance.getStagnantProducts().isEmpty()) {
            document.add(new Paragraph("Produtos Encalhados (60 dias sem venda):").setFont(font).setFontSize(12).setBold().setMarginTop(10));
            Table stagnantTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25})).setWidth(UnitValue.createPercentValue(100));
            stagnantTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            stagnantTable.addCell(new Cell().add(new Paragraph("Estoque Atual").setFont(font).setBold()));
            stagnantTable.addCell(new Cell().add(new Paragraph("Dias Parado").setFont(font).setBold()));

            performance.getStagnantProducts().forEach(p -> {
                stagnantTable.addCell(new Cell().add(new Paragraph(p.getName()).setFont(font)));
                stagnantTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCurrentStock())).setFont(font)));
                stagnantTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getDaysInactive())).setFont(font)));
            });
            document.add(stagnantTable);
        }

        if (performance.getTurnoverRate() != null && !performance.getTurnoverRate().isEmpty()) {
            document.add(new Paragraph("Giro de Estoque:").setFont(font).setFontSize(12).setBold().setMarginTop(10));
            Table turnoverTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25})).setWidth(UnitValue.createPercentValue(100));
            turnoverTable.addCell(new Cell().add(new Paragraph("Produto").setFont(font).setBold()));
            turnoverTable.addCell(new Cell().add(new Paragraph("Vendas (último ano)").setFont(font).setBold()));
            turnoverTable.addCell(new Cell().add(new Paragraph("Giro").setFont(font).setBold()));

            performance.getTurnoverRate().forEach(t -> {
                turnoverTable.addCell(new Cell().add(new Paragraph(t.getProductName()).setFont(font)));
                turnoverTable.addCell(new Cell().add(new Paragraph(String.valueOf(t.getTotalSold())).setFont(font)));
                turnoverTable.addCell(new Cell().add(new Paragraph(String.valueOf(t.getTurnoverRate())).setFont(font)));
            });
            document.add(turnoverTable);
        }

        document.close();
        long duration = System.currentTimeMillis() - startTime;
        log.info("PDF - Relatório gerado com sucesso. Tamanho: {} bytes, Tempo: {}ms", baos.size(), duration);
        return baos.toByteArray();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f", value).replace(".", ",");
    }
}