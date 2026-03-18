package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryId(Long categoryId);
}