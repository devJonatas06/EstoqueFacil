package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.ProductBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface    ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    Page<ProductBatch> findByProductId(Long productId, Pageable pageable);

    @Query("""
        SELECT b FROM ProductBatch b
        WHERE b.expirationDate < :date
        AND b.active = true
        ORDER BY b.expirationDate
        """)
    Page<ProductBatch> findExpiredBatches(@Param("date") LocalDate date, Pageable pageable);

    @Query("""
        SELECT b FROM ProductBatch b
        WHERE b.expirationDate BETWEEN :start AND :end
        AND b.active = true
        ORDER BY b.expirationDate
        """)
    Page<ProductBatch> findExpiringBatches(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable
    );

    List<ProductBatch> findByActiveTrueOrderByExpirationDate();

    @Query("""
        SELECT COALESCE(SUM(b.quantity), 0)
        FROM ProductBatch b
        WHERE b.product.id = :productId
        AND b.active = true
        """)
    Integer getTotalStockByProduct(@Param("productId") Long productId);

    @Query("""
        SELECT b FROM ProductBatch b
        WHERE b.product.id = :productId
        AND b.active = true
        ORDER BY b.entryDate DESC
        """)
    List<ProductBatch> findActiveBatchesByProduct(@Param("productId") Long productId);
}