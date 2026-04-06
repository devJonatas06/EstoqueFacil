package com.example.EstoqueFacil.repository;

import com.example.EstoqueFacil.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> { }
