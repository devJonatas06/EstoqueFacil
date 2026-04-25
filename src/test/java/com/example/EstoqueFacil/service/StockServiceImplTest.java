package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.entity.*;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.StockMapper;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductBatchRepository productBatchRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private UserRepository userRepository;
    @Mock private StockMapper stockMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product product;
    private User user;
    private ProductBatch batch1;
    private ProductBatch batch2;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Produto Teste");
        product.setActive(true);

        user = new User();
        user.setId(1L);
        user.setEmail("teste@email.com");
        user.setActive(true);

        batch1 = new ProductBatch();
        batch1.setId(1L);
        batch1.setProduct(product);
        batch1.setQuantity(10);
        batch1.setExpirationDate(LocalDate.now().plusDays(30));
        batch1.setActive(true);

        batch2 = new ProductBatch();
        batch2.setId(2L);
        batch2.setProduct(product);
        batch2.setQuantity(5);
        batch2.setExpirationDate(LocalDate.now().plusDays(60));
        batch2.setActive(true);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não existe na entrada")
    void shouldThrowExceptionWhenProductNotFoundOnEntry() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        StockEntryDTO entryDTO = new StockEntryDTO();
        entryDTO.setProductId(999L);
        entryDTO.setQuantity(10);
        entryDTO.setUserId(1L);

        assertThatThrownBy(() -> stockService.registerEntry(entryDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto está inativo")
    void shouldThrowExceptionWhenProductIsInactive() {
        product.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        StockEntryDTO entryDTO = new StockEntryDTO();
        entryDTO.setProductId(1L);
        entryDTO.setQuantity(10);
        entryDTO.setUserId(1L);

        assertThatThrownBy(() -> stockService.registerEntry(entryDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Produto inativo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for zero ou negativa na entrada")
    void shouldThrowExceptionWhenQuantityIsInvalid() {
        StockEntryDTO entryDTO = new StockEntryDTO();
        entryDTO.setProductId(1L);
        entryDTO.setQuantity(0);
        entryDTO.setUserId(1L);

        assertThatThrownBy(() -> stockService.registerEntry(entryDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve registrar entrada com sucesso")
    void shouldRegisterEntrySuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productBatchRepository.save(any(ProductBatch.class))).thenReturn(batch1);

        StockEntryDTO entryDTO = new StockEntryDTO();
        entryDTO.setProductId(1L);
        entryDTO.setQuantity(10);
        entryDTO.setExpirationDate(LocalDate.now().plusDays(30));
        entryDTO.setUserId(1L);

        assertThatCode(() -> stockService.registerEntry(entryDTO)).doesNotThrowAnyException();

        verify(productBatchRepository, times(1)).save(any(ProductBatch.class));
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Deve aplicar lógica FIFO na saída")
    void shouldApplyFifoLogicOnExit() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productBatchRepository.getTotalStockByProduct(1L)).thenReturn(15);
        when(productBatchRepository.findByActiveTrueOrderByExpirationDate())
                .thenReturn(Arrays.asList(batch1, batch2));

        StockExitDTO exitDTO = new StockExitDTO();
        exitDTO.setProductId(1L);
        exitDTO.setQuantity(12);
        exitDTO.setType(StockMovementType.SALE);
        exitDTO.setUserId(1L);

        assertThatCode(() -> stockService.registerExit(exitDTO)).doesNotThrowAnyException();

        ArgumentCaptor<ProductBatch> batchCaptor = ArgumentCaptor.forClass(ProductBatch.class);
        verify(productBatchRepository, times(2)).save(batchCaptor.capture());

        List<ProductBatch> savedBatches = batchCaptor.getAllValues();
        assertThat(savedBatches.get(0).getQuantity()).isZero();
        assertThat(savedBatches.get(1).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void shouldThrowExceptionWhenInsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productBatchRepository.getTotalStockByProduct(1L)).thenReturn(5);

        StockExitDTO exitDTO = new StockExitDTO();
        exitDTO.setProductId(1L);
        exitDTO.setQuantity(10);
        exitDTO.setType(StockMovementType.SALE);
        exitDTO.setUserId(1L);

        assertThatThrownBy(() -> stockService.registerExit(exitDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    @DisplayName("Deve lançar exceção para tipo de saída inválido")
    void shouldThrowExceptionForInvalidExitType() {
        StockExitDTO exitDTO = new StockExitDTO();
        exitDTO.setProductId(1L);
        exitDTO.setQuantity(10);
        exitDTO.setType(null);
        exitDTO.setUserId(1L);

        assertThatThrownBy(() -> stockService.registerExit(exitDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Tipo inválido para saída. Use SALE ou LOSS");
    }

    @Test
    @DisplayName("Deve retornar estoque atual do produto")
    void shouldReturnCurrentStock() {
        when(productBatchRepository.getTotalStockByProduct(1L)).thenReturn(42);

        Integer stock = stockService.getCurrentStock(1L);

        assertThat(stock).isEqualTo(42);
        verify(productBatchRepository).getTotalStockByProduct(1L);
    }
}