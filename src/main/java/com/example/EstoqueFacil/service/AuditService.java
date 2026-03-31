package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {

    void log(String action, String entityType, Long entityId, Long userId, String userEmail, Object oldValue, Object newValue);

    void logSimple(String action, String entityType, Long entityId, Long userId, String userEmail, String details);

    Page<AuditLogResponseDTO> findAll(Pageable pageable);

    Page<AuditLogResponseDTO> findByEntity(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogResponseDTO> findByUser(Long userId, Pageable pageable);

    Page<AuditLogResponseDTO> findByAction(String action, Pageable pageable);
}