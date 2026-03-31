// StockMovementRepository.java
package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.dto.product.ProductSalesDTO;
import com.example.EstoqueFacil.dto.report.BestSellingProductDTO;
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
        SELECT sm FROM StockMovement sm
        JOIN FETCH sm.batch b
        JOIN FETCH b.product
        WHERE b.product.id = :productId
        ORDER BY sm.movementDate DESC
        """)
    List<StockMovement> findFullHistoryByProduct(@Param("productId") Long productId);

    // Adicionar no StockMovementRepository.java

    @Query("""
    SELECT sm FROM StockMovement sm
    WHERE sm.batch.product.id = :productId
    AND sm.movementDate BETWEEN :start AND :end
    ORDER BY sm.movementDate DESC
    """)
    List<StockMovement> findByProductAndPeriod(
            @Param("productId") Long productId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT sm.batch.product.id, SUM(sm.quantity * (sm.batch.product.salePrice - sm.batch.product.costPrice))
    FROM StockMovement sm
    WHERE sm.type = 'SALE'
    AND sm.movementDate BETWEEN :start AND :end
    GROUP BY sm.batch.product.id
    ORDER BY SUM(sm.quantity * (sm.batch.product.salePrice - sm.batch.product.costPrice)) DESC
    """)
    List<Object[]> findProfitByProduct(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
    SELECT 
        sm.batch.product.id as productId,
        sm.batch.product.name as productName,
        sm.batch.product.barcode as barcode,
        SUM(sm.quantity) as totalSold,
        SUM(sm.quantity * sm.batch.product.salePrice) as totalRevenue,
        SUM(sm.quantity * (sm.batch.product.salePrice - sm.batch.product.costPrice)) as profit
    FROM StockMovement sm
    WHERE sm.type = 'SALE'
    GROUP BY sm.batch.product.id, sm.batch.product.name, sm.batch.product.barcode
    ORDER BY totalSold DESC
    """)
    List<Object[]> findBestSellingProducts();




}