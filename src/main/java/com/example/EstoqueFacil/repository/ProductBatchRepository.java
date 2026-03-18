package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    List<ProductBatch> findByProductId(Long productId);

    List<ProductBatch> findByExpirationDateBefore(LocalDate date);

    List<ProductBatch> findByExpirationDateBetween(LocalDate start, LocalDate end);

    List<ProductBatch> findByActiveTrue();
}