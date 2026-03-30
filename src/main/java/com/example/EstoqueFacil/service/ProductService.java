package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO create(Product product);

    ProductResponseDTO update(Long id, ProductUpdateDTO updateDTO);

    ProductResponseDTO findById(Long id);

    Page<ProductResponseDTO> findAll(Pageable pageable);

    void deactivate(Long id);
}