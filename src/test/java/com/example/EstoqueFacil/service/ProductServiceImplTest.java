package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductRequestDTO;
import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.ProductMapper;
import com.example.EstoqueFacil.repository.CategoryRepository;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ProductBatchRepository productBatchRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Eletrônicos");
        category.setActive(true);

        product = new Product();
        product.setId(1L);
        product.setName("Smartphone");
        product.setBarcode("7891234567890");
        product.setCostPrice(BigDecimal.valueOf(500.00));
        product.setSalePrice(BigDecimal.valueOf(800.00));
        product.setMinimumStock(10);
        product.setCategory(category);
        product.setActive(true);

        requestDTO = new ProductRequestDTO();
        requestDTO.setName("Smartphone");
        requestDTO.setBarcode("7891234567890");
        requestDTO.setCostPrice(BigDecimal.valueOf(500.00));
        requestDTO.setSalePrice(BigDecimal.valueOf(800.00));
        requestDTO.setMinimumStock(10);
        requestDTO.setCategoryId(1L);
    }

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void shouldCreateProductSuccessfully() {
        when(productRepository.existsByBarcode("7891234567890")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(requestDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(new ProductResponseDTO());

        ProductResponseDTO response = productService.create(requestDTO);

        assertThat(response).isNotNull();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com código de barras duplicado")
    void shouldThrowExceptionWhenDuplicateBarcode() {
        when(productRepository.existsByBarcode("7891234567890")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Código de barras já existe");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com categoria inexistente")
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(productRepository.existsByBarcode("7891234567890")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Deve encontrar produto por ID com sucesso")
    void shouldFindProductByIdSuccessfully() {
        when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));
        when(productBatchRepository.getTotalStockByProduct(1L)).thenReturn(25);
        when(productMapper.toResponseDTO(product)).thenReturn(new ProductResponseDTO());

        ProductResponseDTO response = productService.findById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado por ID")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    @DisplayName("Deve desativar produto com sucesso")
    void shouldDeactivateProductSuccessfully() {
        when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));

        productService.deactivate(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository, times(1)).save(product);
    }
}