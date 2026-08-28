package com.example.EstoqueFacil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;  // CREATE, UPDATE, DELETE, SALE, ENTRY, LOSS

    @Column(nullable = false)
    private String entityType;  // PRODUCT, CATEGORY, STOCK_BATCH, USER, STOCK_MOVEMENT

    private Long entityId;

    private Long userId;

    private String userEmail;

    @Column(columnDefinition = "TEXT")
    private String oldValue;  // JSON com dados antes da alteração

    @Column(columnDefinition = "TEXT")
    private String newValue;  // JSON com dados depois da alteração

    private String details;

    @CreationTimestamp
    private LocalDateTime timestamp;

    public void debug(String s, String action, String email) {
    }
}