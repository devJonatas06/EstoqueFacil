package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.category.CategoryRequestDTO;
import com.example.EstoqueFacil.dto.category.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO create(CategoryRequestDTO requestDTO);

    CategoryResponseDTO update(Long id, CategoryRequestDTO requestDTO);

    CategoryResponseDTO findById(Long id);

    List<CategoryResponseDTO> findAllActive();

    void deactivate(Long id);
}