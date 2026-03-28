package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Product create(Product product);

    Product update(Long id, Product product);

    Product findById(Long id);

    Page<Product> findAll(Pageable pageable);

    void deactivate(Long id);
}