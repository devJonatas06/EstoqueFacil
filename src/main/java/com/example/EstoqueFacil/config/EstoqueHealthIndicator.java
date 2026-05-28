package com.example.EstoqueFacil.config;

import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.entity.StockMovementType;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Lazy
public class EstoqueHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(EstoqueHealthIndicator.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBatchRepository batchRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    // ===== CACHES SEPARADOS =====
    private final ConcurrentHashMap<String, CachedValue> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedBestSellers> bestSellersCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

    private static class CachedValue {
        final long value;
        final long timestamp;

        CachedValue(long value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    private static class CachedBestSellers {
        final List<Object[]> value;
        final long timestamp;

        CachedBestSellers(List<Object[]> value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TimeUnit.MINUTES.toMillis(2);
        }
    }

    private long getCachedOrCompute(String key, CacheableSupplier supplier) {
        CachedValue cached = cache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        long newValue = supplier.get();
        cache.put(key, new CachedValue(newValue));
        return newValue;
    }

    private List<Object[]> getCachedBestSellers() {
        CachedBestSellers cached = bestSellersCache.get("produtos_mais_vendidos");
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        List<Object[]> bestSellers = stockMovementRepository.findBestSellingProducts();
        bestSellersCache.put("produtos_mais_vendidos", new CachedBestSellers(bestSellers));
        return bestSellers;
    }

    @FunctionalInterface
    private interface CacheableSupplier {
        long get();
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Com cache - queries pesadas
            long produtosComEstoqueBaixo = getCachedOrCompute("produtos_estoque_baixo", this::countProductsWithLowStock);
            long lotesVencidos = getCachedOrCompute("lotes_vencidos", this::countExpiredBatches);
            long lotesProximosVencimento = getCachedOrCompute("lotes_proximos_vencer", () -> countExpiringBatches(7));
            long diasSemMovimentacao = getCachedOrCompute("dias_sem_movimentacao", this::daysSinceLastMovement);
            long produtosSemMovimentacao = getCachedOrCompute("produtos_sem_movimentacao", () -> countProductsWithoutMovement(30));
            long produtosAtivos = getCachedOrCompute("produtos_ativos", this::countActiveProducts);

            // Sem cache - queries leves (executam toda vez)
            long produtosVendidosHoje = countProductsSoldToday();
            double lucroEstimadoDia = calculateEstimatedProfit(LocalDateTime.now().minusDays(1), LocalDateTime.now());
            double lucroEstimadoMes = calculateEstimatedProfit(LocalDateTime.now().minusDays(30), LocalDateTime.now());

            // Top produtos com cache separado
            List<Object[]> produtosMaisVendidos = getCachedBestSellers();

            // Registrar métricas para o Prometheus
            meterRegistry.gauge("estoque.produtos.ativos", produtosAtivos);
            meterRegistry.gauge("estoque.produtos.baixo", produtosComEstoqueBaixo);
            meterRegistry.gauge("estoque.lotes.vencidos", lotesVencidos);
            meterRegistry.gauge("estoque.lotes.proximos.vencer", lotesProximosVencimento);
            meterRegistry.gauge("estoque.dias.sem.movimentacao", diasSemMovimentacao);
            meterRegistry.gauge("estoque.produtos.parados", produtosSemMovimentacao);
            meterRegistry.gauge("estoque.lucro.estimado.mes", lucroEstimadoMes);
            meterRegistry.gauge("estoque.vendas.diarias", produtosVendidosHoje);
            meterRegistry.gauge("estoque.lucro.diario", lucroEstimadoDia);

            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {
                log.warn("⚠️ Health check lento: {}ms", duration);
            }

            // 🔴 CRÍTICO: Lotes vencidos
            if (lotesVencidos > 0) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("lotes_vencidos", lotesVencidos)
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("lotes_proximos_vencer", lotesProximosVencimento)
                        .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                        .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                        .withDetail("tempo_health_ms", duration)
                        .withDetail("mensagem", "⚠️ Existem lotes VENCIDOS no sistema! Ação imediata necessária.")
                        .build();
            }

            // 🟡 ATENÇÃO: Lotes próximos ao vencimento
            if (lotesProximosVencimento > 0) {
                return Health.status("WARNING")
                        .withDetail("status", "WARNING")
                        .withDetail("lotes_proximos_vencer", lotesProximosVencimento)
                        .withDetail("lotes_vencidos", lotesVencidos)
                        .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                        .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                        .withDetail("tempo_health_ms", duration)
                        .withDetail("mensagem", "⚠️ Existem lotes que vencem nos próximos 7 dias!")
                        .build();
            }

            // 🔴 CRÍTICO: Estoque muito baixo (>10 produtos)
            if (produtosComEstoqueBaixo > 10) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("produtos_parados", produtosSemMovimentacao)
                        .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                        .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                        .withDetail("tempo_health_ms", duration)
                        .withDetail("mensagem", "⚠️ Mais de 10 produtos com estoque abaixo do mínimo!")
                        .build();
            }

            // 🟡 DEGRADADO: Estoque baixo (1-10 produtos)
            if (produtosComEstoqueBaixo > 0) {
                return Health.status("DEGRADED")
                        .withDetail("status", "DEGRADED")
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("produtos_parados", produtosSemMovimentacao)
                        .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                        .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                        .withDetail("tempo_health_ms", duration)
                        .withDetail("mensagem", "📦 Existem produtos com estoque abaixo do mínimo")
                        .build();
            }

            // 🟡 ATENÇÃO: Produtos parados há mais de 30 dias
            if (produtosSemMovimentacao > 0) {
                return Health.status("DEGRADED")
                        .withDetail("status", "DEGRADED")
                        .withDetail("produtos_parados", produtosSemMovimentacao)
                        .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                        .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                        .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                        .withDetail("tempo_health_ms", duration)
                        .withDetail("mensagem", "📦 Existem produtos sem movimentação há mais de 30 dias")
                        .build();
            }

            // 🟢 TUDO SAUDÁVEL
            return Health.up()
                    .withDetail("status", "HEALTHY")
                    .withDetail("produtos_ativos", produtosAtivos)
                    .withDetail("produtos_estoque_baixo", 0)
                    .withDetail("lotes_vencidos", 0)
                    .withDetail("lotes_proximos_vencer", 0)
                    .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                    .withDetail("produtos_parados", 0)
                    .withDetail("lucro_estimado_mes", String.format("R$ %.2f", lucroEstimadoMes))
                    .withDetail("produtos_vendidos_hoje", produtosVendidosHoje)
                    .withDetail("lucro_estimado_dia", String.format("R$ %.2f", lucroEstimadoDia))
                    .withDetail("top_3_produtos_mais_vendidos", formatTopProducts(produtosMaisVendidos))
                    .withDetail("tempo_health_ms", duration)
                    .withDetail("mensagem", "✅ Estoque saudável. Todos os indicadores dentro da normalidade.")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao verificar saúde do estoque: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    // ===== MÉTODOS ORIGINAIS (SEM CACHE) =====

    private long countProductsWithLowStock() {
        try {
            return productRepository.findBelowMinimumStock().size();
        } catch (Exception e) {
            log.warn("Não foi possível contar produtos com estoque baixo: {}", e.getMessage());
            return 0;
        }
    }

    private long countActiveProducts() {
        try {
            return productRepository.findByActiveTrue(Pageable.unpaged()).getTotalElements();
        } catch (Exception e) {
            log.warn("Não foi possível contar produtos ativos: {}", e.getMessage());
            return 0;
        }
    }

    private long countExpiredBatches() {
        try {
            List<ProductBatch> expiredBatches = batchRepository.findExpiredBatches(LocalDate.now());
            return expiredBatches.size();
        } catch (Exception e) {
            log.warn("Não foi possível contar lotes vencidos: {}", e.getMessage());
            return 0;
        }
    }

    private long countExpiringBatches(int daysThreshold) {
        try {
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusDays(daysThreshold);
            List<ProductBatch> expiringBatches = batchRepository.findExpiringBatchesBetween(start, end);
            return expiringBatches.size();
        } catch (Exception e) {
            log.warn("Não foi possível contar lotes próximos ao vencimento: {}", e.getMessage());
            return 0;
        }
    }

    private long daysSinceLastMovement() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            var lastMovements = stockMovementRepository.findByTypeAndPeriod(
                    StockMovementType.SALE,
                    thirtyDaysAgo,
                    LocalDateTime.now(),
                    Pageable.ofSize(1)
            );

            if (lastMovements.hasContent()) {
                LocalDateTime lastMovement = lastMovements.getContent().get(0).getMovementDate();
                long days = java.time.Duration.between(lastMovement, LocalDateTime.now()).toDays();
                return Math.max(days, 0);
            }
            return 30;
        } catch (Exception e) {
            log.warn("Não foi possível calcular dias sem movimentação: {}", e.getMessage());
            return -1;
        }
    }

    private long countProductsWithoutMovement(int daysThreshold) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(daysThreshold);
            return productRepository.findProductsWithoutMovementSince(since).size();
        } catch (Exception e) {
            log.warn("Não foi possível contar produtos sem movimentação: {}", e.getMessage());
            return 0;
        }
    }

    private double calculateEstimatedProfit(LocalDateTime start, LocalDateTime end) {
        try {
            List<Object[]> profits = stockMovementRepository.findProfitByProduct(start, end);
            return profits.stream()
                    .mapToDouble(p -> ((Number) p[1]).doubleValue())
                    .sum();
        } catch (Exception e) {
            log.warn("Não foi possível calcular lucro estimado: {}", e.getMessage());
            return 0;
        }
    }

    private long countProductsSoldToday() {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            var movements = stockMovementRepository.findByTypeAndPeriod(
                    StockMovementType.SALE,
                    startOfDay,
                    endOfDay,
                    Pageable.unpaged()
            );

            return movements.stream()
                    .mapToLong(m -> m.getQuantity())
                    .sum();
        } catch (Exception e) {
            log.warn("Não foi possível contar produtos vendidos hoje: {}", e.getMessage());
            return 0;
        }
    }

    private String formatTopProducts(List<Object[]> bestSellers) {
        if (bestSellers == null || bestSellers.isEmpty()) {
            return "Nenhum produto vendido ainda";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(3, bestSellers.size());

        for (int i = 0; i < limit; i++) {
            Object[] product = bestSellers.get(i);
            String name = (String) product[1];
            Long quantity = ((Number) product[3]).longValue();

            if (i > 0) sb.append(" | ");
            sb.append(String.format("%dº: %s (%d unidades)", i + 1, name, quantity));
        }

        return sb.toString();
    }
}