package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("""
        SELECT c FROM Category c
        WHERE c.active = true
        ORDER BY c.name
        """)
    List<Category> findAllActiveOrdered();

    @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.products
        WHERE c.id = :id
        """)
    Optional<Category> findByIdWithProducts(@Param("id") Long id);
}