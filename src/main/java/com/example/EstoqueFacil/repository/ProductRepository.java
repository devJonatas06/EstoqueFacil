package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
        """)
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.minimumStock > (
            SELECT COALESCE(SUM(b.quantity), 0)
            FROM ProductBatch b
            WHERE b.product = p AND b.active = true
        )
        """)
    List<Product> findBelowMinimumStock();

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        WHERE p.id = :id
        """)
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    // Adicionar no ProductRepository.java

    @Query("""
    SELECT p FROM Product p
    WHERE p.active = true
    AND NOT EXISTS (
        SELECT sm FROM StockMovement sm
        WHERE sm.batch.product.id = p.id
        AND sm.type = 'SALE'
        AND sm.movementDate >= :since
    )
    """)
    List<Product> findProductsWithoutMovementSince(@Param("since") LocalDateTime since);

    @Query("""
    SELECT p FROM Product p
    WHERE p.active = true
    AND p.minimumStock > (
        SELECT COALESCE(SUM(b.quantity), 0) 
        FROM ProductBatch b 
        WHERE b.product.id = p.id 
        AND b.active = true
    )
    AND (
        SELECT MIN(sm.movementDate) 
        FROM StockMovement sm 
        WHERE sm.batch.product.id = p.id
    ) <= :since
    """)
    List<Product> findCriticalStockSince(@Param("since") LocalDateTime since);
}