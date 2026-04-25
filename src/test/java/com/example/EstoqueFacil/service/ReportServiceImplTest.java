package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.*;
import com.example.EstoqueFacil.entity.*;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private ProductBatchRepository productBatchRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Product product;
    private ProductBatch batch;
    private StockMovement saleMovement;
    private StockMovement entryMovement;
    private User user;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Produto Teste");
        product.setBarcode("123456");
        product.setCostPrice(BigDecimal.valueOf(50.00));
        product.setSalePrice(BigDecimal.valueOf(80.00));

        batch = new ProductBatch();
        batch.setId(1L);
        batch.setProduct(product);
        batch.setQuantity(100);
        batch.setExpirationDate(LocalDate.now().plusDays(30));

        user = new User();
        user.setId(1L);
        user.setName("Admin Teste");

        saleMovement = new StockMovement();
        saleMovement.setId(1L);
        saleMovement.setBatch(batch);
        saleMovement.setQuantity(10);
        saleMovement.setType(StockMovementType.SALE);
        saleMovement.setMovementDate(LocalDateTime.now());
        saleMovement.setUser(user);

        entryMovement = new StockMovement();
        entryMovement.setId(2L);
        entryMovement.setBatch(batch);
        entryMovement.setQuantity(50);
        entryMovement.setType(StockMovementType.ENTRY);
        entryMovement.setMovementDate(LocalDateTime.now());
        entryMovement.setUser(user);
    }

    @Test
    @DisplayName("Deve retornar produtos mais vendidos")
    void shouldReturnBestSellingProducts() {
        Object[] result = new Object[]{
                1L, "Produto Teste", "123456", 100L,
                BigDecimal.valueOf(8000), BigDecimal.valueOf(3000)
        };
        when(stockMovementRepository.findBestSellingProducts()).thenReturn(Arrays.<Object[]>asList(result));

        List<BestSellingProductDTO> bestSellers = reportService.getBestSellingProducts();

        assertThat(bestSellers).isNotEmpty();
        assertThat(bestSellers.get(0).getProductName()).isEqualTo("Produto Teste");
    }

    @Test
    @DisplayName("Deve calcular relatório de lucro corretamente")
    void shouldCalculateProfitReportCorrectly() {
        when(stockMovementRepository.findByTypeAndPeriod(eq(StockMovementType.SALE), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(saleMovement)));

        ProfitReportDTO profitReport = reportService.getProfitReport(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        assertThat(profitReport.getTotalProfit()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(profitReport.getTotalItemsSold()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve retornar lotes próximos ao vencimento")
    void shouldReturnExpiringBatches() {
        when(productBatchRepository.findExpiringBatchesBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(batch));

        List<ExpiringBatchDTO> expiringBatches = reportService.getExpiringBatches(30);

        assertThat(expiringBatches).isNotEmpty();
        assertThat(expiringBatches.get(0).getProductName()).isEqualTo("Produto Teste");
        assertThat(expiringBatches.get(0).getDaysToExpire()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar movimentações por período")
    void shouldReturnMovementsByPeriod() {
        when(stockMovementRepository.findByMovementDateBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(saleMovement, entryMovement)));

        List<StockMovementReportDTO> movements = reportService.getMovementsByPeriod(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        assertThat(movements).hasSize(2);
    }

    @Test
    @DisplayName("Deve gerar relatório de perdas")
    void shouldGenerateLossReport() {
        when(productBatchRepository.findExpiredBatches(any(LocalDate.class)))
                .thenReturn(Arrays.asList(batch));

        LossReportDTO lossReport = reportService.getLossReport();

        assertThat(lossReport.getTotalExpiredUnits()).isEqualTo(100);
        assertThat(lossReport.getTotalEstimatedLoss()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }
}