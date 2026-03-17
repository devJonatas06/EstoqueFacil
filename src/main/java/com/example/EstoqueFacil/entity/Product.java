package com.example.EstoqueFacil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    private String maker;

    private String description;

    @Column(unique = true,nullable = false)
    private String barcode;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;
    @Column(nullable = false)
    private Integer minimumStock;
    @Column(nullable = false)
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
