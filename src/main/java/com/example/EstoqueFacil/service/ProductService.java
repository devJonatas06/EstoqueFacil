package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductFilterDTO;
import com.example.EstoqueFacil.dto.product.ProductRequestDTO;
import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO create(ProductRequestDTO requestDTO);

    ProductResponseDTO update(Long id, ProductUpdateDTO updateDTO);

    ProductResponseDTO findById(Long id);

    Page<ProductResponseDTO> findAll(Pageable pageable);

    Page<ProductResponseDTO> searchByName(String name, Pageable pageable);

    Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable);

    ProductResponseDTO findByBarcode(String barcode);

    void deactivate(Long id);

    Page<ProductResponseDTO> filter(ProductFilterDTO filter, Pageable pageable);
}