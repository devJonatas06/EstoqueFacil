package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.model.dto.request.product.ProductFilterDTO;
import com.example.EstoqueFacil.model.dto.request.product.ProductCreateRequestDTO;
import com.example.EstoqueFacil.model.dto.response.product.ProductResponseDTO;
import com.example.EstoqueFacil.model.dto.request.product.ProductUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO create(ProductCreateRequestDTO requestDTO);

    ProductResponseDTO update(Long id, ProductUpdateRequestDTO updateDTO);

    ProductResponseDTO findById(Long id);

    Page<ProductResponseDTO> findAll(Pageable pageable);

    Page<ProductResponseDTO> searchByName(String name, Pageable pageable);

    Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable);

    ProductResponseDTO findByBarcode(String barcode);

    void deactivate(Long id);

    Page<ProductResponseDTO> filter(ProductFilterDTO filter, Pageable pageable);
}