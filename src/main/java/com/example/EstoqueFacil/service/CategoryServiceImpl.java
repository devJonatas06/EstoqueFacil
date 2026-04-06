package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.category.CategoryRequestDTO;
import com.example.EstoqueFacil.dto.category.CategoryResponseDTO;
import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.CategoryMapper;
import com.example.EstoqueFacil.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO create(CategoryRequestDTO requestDTO) {
        if (categoryRepository.existsByName(requestDTO.getName())) {
            log.warn("Categoria - Tentativa de criar categoria duplicada: {}", requestDTO.getName());
            throw new BusinessException("Categoria já existe: " + requestDTO.getName());
        }

        Category category = categoryMapper.toEntity(requestDTO);
        category.setActive(true);

        Category saved = categoryRepository.save(category);
        log.info("Categoria - Criada com sucesso. ID: {}, Nome: {}", saved.getId(), saved.getName());
        return categoryMapper.toResponseDTO(saved);
    }

    @Override
    public CategoryResponseDTO update(Long id, CategoryRequestDTO requestDTO) {
        Category category = findByIdEntity(id);

        if (!category.getName().equals(requestDTO.getName()) && categoryRepository.existsByName(requestDTO.getName())) {
            log.warn("Categoria - Tentativa de atualizar para nome duplicado. ID: {}, Nome: {}", id, requestDTO.getName());
            throw new BusinessException("Categoria já existe: " + requestDTO.getName());
        }

        categoryMapper.updateEntity(category, requestDTO);
        Category updated = categoryRepository.save(category);
        log.info("Categoria - Atualizada com sucesso. ID: {}, Nome: {}", updated.getId(), updated.getName());
        return categoryMapper.toResponseDTO(updated);
    }

    @Override
    public CategoryResponseDTO findById(Long id) {
        Category category = findByIdEntity(id);
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    public List<CategoryResponseDTO> findAllActive() {
        return categoryRepository.findAllActiveOrdered()
                .stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivate(Long id) {
        Category category = findByIdEntity(id);
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Categoria - Desativada com sucesso. ID: {}, Nome: {}", id, category.getName());
    }

    private Category findByIdEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria - Não encontrada. ID: {}", id);
                    return new ResourceNotFoundException("Categoria não encontrada com ID: " + id);
                });
    }
}