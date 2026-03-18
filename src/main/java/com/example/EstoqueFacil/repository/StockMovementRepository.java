// StockMovementRepository.java
package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.dto.product.ProductSalesDTO;
import com.example.EstoqueFacil.entity.StockMovement;
import com.example.EstoqueFacil.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {


    Page<StockMovement> findByType(StockMovementType type, Pageable pageable);

    Page<StockMovement> findByMovementDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<StockMovement> findByUserId(Long userId, Pageable pageable);

    Page<StockMovement> findByBatchProductId(Long productId, Pageable pageable);
    @Query("""
        SELECT sm FROM StockMovement sm
        WHERE sm.type = :type
        AND sm.movementDate BETWEEN :start AND :end
        ORDER BY sm.movementDate DESC
        """)
    Page<StockMovement> findByTypeAndPeriod(
            @Param("type") StockMovementType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


    @Query("""
        SELECT 
            sm.batch.product.id as productId,
            SUM(sm.quantity) as totalSold
        FROM StockMovement sm
        WHERE sm.type = 'SALE'
        GROUP BY sm.batch.product.id
        ORDER BY totalSold DESC
        """)
    List<ProductSalesDTO> findMostSoldProducts();


    @Query("""
        SELECT sm FROM StockMovement sm
        JOIN FETCH sm.batch b
        JOIN FETCH b.product
        WHERE b.product.id = :productId
        ORDER BY sm.movementDate DESC
        """)
    List<StockMovement> findFullHistoryByProduct(@Param("productId") Long productId);
}