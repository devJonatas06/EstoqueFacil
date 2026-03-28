package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.AuditLog;
import com.example.EstoqueFacil.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(String action, String entityType, Long entityId, Long userId, String userEmail, Object oldValue, Object newValue) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);

            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Erro ao salvar auditoria: {}", e.getMessage());
        }
    }

    public void logSimple(String action, String entityType, Long entityId, Long userId, String userEmail, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);
            auditLog.setDetails(details);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Erro ao salvar auditoria: {}", e.getMessage());
        }
    }
}