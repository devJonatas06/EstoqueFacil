package com.example.EstoqueFacil.config;

import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
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

    @Override
    public Health health() {
        try {
            long produtosComEstoqueBaixo = countProductsWithLowStock();
            long lotesVencidos = countExpiredBatches();
            long diasSemMovimentacao = daysSinceLastMovement();

            // Registrar métricas para o Prometheus
            meterRegistry.gauge("estoque.produtos.ativos", countActiveProducts());
            meterRegistry.gauge("estoque.produtos.baixo", produtosComEstoqueBaixo);
            meterRegistry.gauge("estoque.lotes.vencidos", lotesVencidos);
            meterRegistry.gauge("estoque.dias.sem.movimentacao", diasSemMovimentacao);

            if (produtosComEstoqueBaixo > 10 && lotesVencidos > 0) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("lotes_vencidos", lotesVencidos)
                        .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                        .withDetail("mensagem", "Múltiplos problemas críticos no estoque!")
                        .build();
            }

            if (produtosComEstoqueBaixo > 10) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                        .withDetail("mensagem", "Mais de 10 produtos com estoque abaixo do mínimo!")
                        .build();
            }

            if (lotesVencidos > 0) {
                return Health.down()
                        .withDetail("status", "CRITICAL")
                        .withDetail("lotes_vencidos", lotesVencidos)
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("mensagem", "Existem lotes vencidos no sistema!")
                        .build();
            }

            if (produtosComEstoqueBaixo > 0) {
                return Health.status("DEGRADED")
                        .withDetail("status", "DEGRADED")
                        .withDetail("produtos_estoque_baixo", produtosComEstoqueBaixo)
                        .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                        .withDetail("mensagem", "Existem produtos com estoque abaixo do mínimo")
                        .build();
            }

            return Health.up()
                    .withDetail("status", "HEALTHY")
                    .withDetail("produtos_ativos", countActiveProducts())
                    .withDetail("produtos_estoque_baixo", 0)
                    .withDetail("lotes_vencidos", 0)
                    .withDetail("dias_sem_movimentacao", diasSemMovimentacao)
                    .withDetail("mensagem", "Estoque saudável")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao verificar saúde do estoque: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("status", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

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

    private long daysSinceLastMovement() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            var lastMovements = stockMovementRepository.findByTypeAndPeriod(
                    com.example.EstoqueFacil.entity.StockMovementType.SALE,
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
}