package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.category.CategoryRequestDTO;
import com.example.EstoqueFacil.dto.category.CategoryResponseDTO;
import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.CategoryMapper;
import com.example.EstoqueFacil.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequestDTO requestDTO;
    private CategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Eletrônicos");
        category.setActive(true);

        requestDTO = new CategoryRequestDTO();
        requestDTO.setName("Eletrônicos");

        responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Eletrônicos");
        responseDTO.setActive(true);
    }

    @Test
    @DisplayName("Deve criar categoria com sucesso")
    void shouldCreateCategorySuccessfully() {
        when(categoryRepository.existsByName("Eletrônicos")).thenReturn(false);
        when(categoryMapper.toEntity(requestDTO)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponseDTO(category)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Eletrônicos");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar categoria com nome duplicado")
    void shouldThrowExceptionWhenDuplicateName() {
        when(categoryRepository.existsByName("Eletrônicos")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Categoria já existe");
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void shouldUpdateCategorySuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponseDTO(category)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.update(1L, requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Eletrônicos");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(999L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Deve desativar categoria com sucesso")
    void shouldDeactivateCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.deactivate(1L);

        assertThat(category.isActive()).isFalse();
        verify(categoryRepository, times(1)).save(category);
    }
}