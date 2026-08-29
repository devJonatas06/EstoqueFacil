package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.model.dto.response.report.*;
import com.example.EstoqueFacil.model.entity.Product;
import com.example.EstoqueFacil.model.entity.ProductBatch;
import com.example.EstoqueFacil.messaging.event.BatchExpiredEvent;
import com.example.EstoqueFacil.messaging.event.LowStockEvent;
import com.example.EstoqueFacil.messaging.producer.NotificacaoProducer;
import com.example.EstoqueFacil.messaging.event.ProductStopEvent;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private static final int DIAS_INATIVIDADE_PADRAO = 30;

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NotificacaoProducer notificacaoProducer;

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

    /**
     * Ponto de entrada da notificação: toda vez que o resumo de alertas é
     * calculado, os problemas encontrados viram eventos publicados no
     * RabbitMQ. A publicação nunca impede o resumo de ser retornado —
     * cada bloco é isolado com try/catch dentro dos métodos privados de
     * publicação.
     */
    @Override
    public AlertSummaryDTO getAlertSummary() {
        List<Product> lowStock = getLowStockProducts();
        List<Product> inactive = getInactiveProducts(DIAS_INATIVIDADE_PADRAO);
        List<ProductBatch> expiring = getExpiringBatches(30);
        List<ProductBatch> expired = getExpiredBatches();
        List<Product> critical = getCriticalStockProducts(7);

        if (!lowStock.isEmpty()) {
            log.warn("Alerta - {} produtos com estoque abaixo do mínimo", lowStock.size());
            publicarEventosEstoqueBaixo(lowStock);
        }
        if (!critical.isEmpty()) {
            log.warn("Alerta - {} produtos em situação CRÍTICA", critical.size());
        }
        if (!expired.isEmpty()) {
            log.warn("Alerta - {} lotes vencidos encontrados", expired.size());
            publicarEventosLoteVencido(expired);
        }
        if (!inactive.isEmpty()) {
            publicarEventosProdutoParado(inactive, DIAS_INATIVIDADE_PADRAO);
        }

        return AlertSummaryDTO.builder()
                .lowStockCount(lowStock.size())
                .inactiveProductsCount(inactive.size())
                .expiringSoonCount(expiring.size())
                .expiredCount(expired.size())
                .criticalStockCount(critical.size())
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

    @Override
    public List<LowStockProductDTO> getLowStockProductsDTO() {
        return convertToLowStockDTO(getLowStockProducts());
    }

    // ---------- Publicação de eventos de notificação ----------

    private void publicarEventosEstoqueBaixo(List<Product> produtos) {
        for (Product produto : produtos) {
            try {
                LowStockEvent evento = new LowStockEvent(
                        produto.getId(),
                        produto.getName(),
                        getCurrentStock(produto.getId()),
                        produto.getMinimumStock(),
                        LocalDateTime.now());
                notificacaoProducer.publicarEstoqueBaixo(evento);
            } catch (Exception e) {
                log.error("Falha ao montar/publicar evento de estoque baixo para produtoId: {}. Erro: {}",
                        produto.getId(), e.getMessage(), e);
            }
        }
    }

    private void publicarEventosLoteVencido(List<ProductBatch> lotes) {
        for (ProductBatch lote : lotes) {
            try {
                BatchExpiredEvent evento = new BatchExpiredEvent(
                        lote.getProduct().getId(),
                        lote.getProduct().getName(),
                        lote.getId(),
                        lote.getExpirationDate(),
                        lote.getQuantity(),
                        LocalDateTime.now());
                notificacaoProducer.publicarLoteVencido(evento);
            } catch (Exception e) {
                log.error("Falha ao montar/publicar evento de lote vencido para loteId: {}. Erro: {}",
                        lote.getId(), e.getMessage(), e);
            }
        }
    }

    private void publicarEventosProdutoParado(List<Product> produtos, int diasSemMovimentacao) {
        for (Product produto : produtos) {
            try {
                ProductStopEvent evento = new ProductStopEvent(
                        produto.getId(),
                        produto.getName(),
                        (long) diasSemMovimentacao,
                        produto.getSalePrice() != null ? produto.getSalePrice().doubleValue() : null,
                        LocalDateTime.now());
                notificacaoProducer.publicarProdutoParado(evento);
            } catch (Exception e) {
                log.error("Falha ao montar/publicar evento de produto parado para produtoId: {}. Erro: {}",
                        produto.getId(), e.getMessage(), e);
            }
        }
    }

    // ---------- Conversões existentes (sem alteração de comportamento) ----------

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
                        .daysBelowMinimum(5)
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
                        .lastMovementDate(LocalDateTime.now().minusDays(10))
                        .daysInactive(15)
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
                        .daysExpired((int) b.getExpirationDate().until(today).getDays())
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
                        .daysBelowMinimum(7)
                        .build())
                .collect(Collectors.toList());
    }

    private Integer getCurrentStock(Long productId) {
        return productBatchRepository.getTotalStockByProduct(productId);
    }

    private String getStockStatus(Product product) {
        Integer currentStock = getCurrentStock(product.getId());
        Integer minimumStock = product.getMinimumStock();

        if (currentStock >= minimumStock) return "OK";
        if (currentStock >= minimumStock * 0.5) return "BAIXO";
        return "CRÍTICO";
    }

    private String getExpiringStatus(LocalDate expirationDate) {
        LocalDate today = LocalDate.now();
        long daysToExpire = today.until(expirationDate).getDays();

        if (daysToExpire < 7) return "URGENTE";
        if (daysToExpire < 30) return "ATENÇÃO";
        return "OK";
    }
}