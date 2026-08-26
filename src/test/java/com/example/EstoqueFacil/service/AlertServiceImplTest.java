package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AlertDetailDTO;
import com.example.EstoqueFacil.dto.report.AlertSummaryDTO;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import com.example.EstoqueFacil.event.NotificacaoProducer;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Cobre AlertServiceImpl, incluindo a publicação de eventos de notificação
 * adicionada em getAlertSummary(). Os setters de Product/ProductBatch
 * assumem os mesmos nomes usados nos getters já presentes na classe de
 * produção (padrão Lombok @Data) — ajuste se os seus forem diferentes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertServiceImpl")
class AlertServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private NotificacaoProducer notificacaoProducer;

    private AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(
                productRepository, productBatchRepository, stockMovementRepository, notificacaoProducer);
    }

    private Product produtoBaixo() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Produto Baixo");
        p.setBarcode("111");
        p.setMinimumStock(20);
        p.setSalePrice(BigDecimal.valueOf(9.90));
        return p;
    }

    private ProductBatch loteVencido() {
        Product produto = new Product();
        produto.setId(2L);
        produto.setName("Produto Vencido");
        produto.setBarcode("222");
        produto.setCostPrice(BigDecimal.valueOf(3.0));

        ProductBatch lote = new ProductBatch();
        lote.setId(50L);
        lote.setProduct(produto);
        lote.setExpirationDate(LocalDate.now().minusDays(2));
        lote.setQuantity(10);
        return lote;
    }

    private void semAlertas() {
        when(productRepository.findProductsWithoutMovementSince(any())).thenReturn(List.of());
        when(productBatchRepository.findExpiringBatchesBetween(any(), any())).thenReturn(List.of());
        when(productBatchRepository.findExpiredBatches(any())).thenReturn(List.of());
        when(productRepository.findCriticalStockSince(any())).thenReturn(List.of());
    }

    @Nested
    @DisplayName("getAlertSummary")
    class GetAlertSummary {

        @Test
        @DisplayName("publica um evento de estoque baixo para cada produto encontrado")
        void publicaEventoParaCadaProdutoComEstoqueBaixo() {
            Product produto = produtoBaixo();
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of(produto));
            semAlertas();
            when(productBatchRepository.getTotalStockByProduct(produto.getId())).thenReturn(5);

            AlertSummaryDTO resumo = alertService.getAlertSummary();

            assertThat(resumo.getLowStockCount()).isEqualTo(1);
            verify(notificacaoProducer, times(1)).publicarEstoqueBaixo(argThat(evento ->
                    evento.getProdutoId().equals(produto.getId())
                            && evento.getQuantidadeAtual() == 5
                            && evento.getQuantidadeMinima() == 20));
        }

        @Test
        @DisplayName("publica um evento de lote vencido para cada lote encontrado")
        void publicaEventoParaCadaLoteVencido() {
            ProductBatch lote = loteVencido();
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of());
            when(productRepository.findProductsWithoutMovementSince(any())).thenReturn(List.of());
            when(productBatchRepository.findExpiringBatchesBetween(any(), any())).thenReturn(List.of());
            when(productBatchRepository.findExpiredBatches(any())).thenReturn(List.of(lote));
            when(productRepository.findCriticalStockSince(any())).thenReturn(List.of());

            alertService.getAlertSummary();

            verify(notificacaoProducer, times(1)).publicarLoteVencido(argThat(evento ->
                    evento.getLoteId().equals(lote.getId())
                            && evento.getProdutoId().equals(lote.getProduct().getId())));
        }

        @Test
        @DisplayName("não publica nada quando não há problemas")
        void naoPublicaQuandoTudoOk() {
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of());
            semAlertas();

            alertService.getAlertSummary();

            verifyNoInteractions(notificacaoProducer);
        }

        @Test
        @DisplayName("continua retornando o resumo mesmo se a publicação do evento falhar")
        void naoQuebraQuandoPublicacaoFalha() {
            Product produto = produtoBaixo();
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of(produto));
            semAlertas();
            when(productBatchRepository.getTotalStockByProduct(produto.getId())).thenReturn(5);
            doThrow(new RuntimeException("broker fora do ar"))
                    .when(notificacaoProducer).publicarEstoqueBaixo(any());

            AlertSummaryDTO resumo = alertService.getAlertSummary();

            assertThat(resumo.getLowStockCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("consultas e conversões para DTO")
    class Consultas {

        @Test
        void getLowStockProductsDelegaParaRepository() {
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of(produtoBaixo()));

            List<Product> resultado = alertService.getLowStockProducts();

            assertThat(resultado).hasSize(1);
            verify(productRepository).findBelowMinimumStock();
        }

        @Test
        void getExpiredBatchesDelegaParaRepository() {
            when(productBatchRepository.findExpiredBatches(any())).thenReturn(List.of(loteVencido()));

            List<ProductBatch> resultado = alertService.getExpiredBatches();

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("getAlertDetails monta todos os blocos (cobre as conversões para DTO)")
        void getAlertDetailsMontaTodosOsBlocos() {
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of(produtoBaixo()));
            when(productRepository.findProductsWithoutMovementSince(any())).thenReturn(List.of(produtoBaixo()));
            when(productBatchRepository.findExpiringBatchesBetween(any(), any())).thenReturn(List.of(loteVencido()));
            when(productBatchRepository.findExpiredBatches(any())).thenReturn(List.of(loteVencido()));
            when(productRepository.findCriticalStockSince(any())).thenReturn(List.of(produtoBaixo()));
            when(productBatchRepository.getTotalStockByProduct(anyLong())).thenReturn(5);

            AlertDetailDTO detalhes = alertService.getAlertDetails();

            assertThat(detalhes.getLowStockProducts()).hasSize(1);
            assertThat(detalhes.getInactiveProducts()).hasSize(1);
            assertThat(detalhes.getExpiringBatches()).hasSize(1);
            assertThat(detalhes.getExpiredBatches()).hasSize(1);
            assertThat(detalhes.getCriticalStockProducts()).hasSize(1);
        }

        @Test
        void getLowStockProductsDTORetornaListaConvertida() {
            when(productRepository.findBelowMinimumStock()).thenReturn(List.of(produtoBaixo()));
            when(productBatchRepository.getTotalStockByProduct(anyLong())).thenReturn(5);

            assertThat(alertService.getLowStockProductsDTO()).hasSize(1);
        }
    }
}