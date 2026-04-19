package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import com.example.EstoqueFacil.entity.AuditLog;
import com.example.EstoqueFacil.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
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
            log.debug("Auditoria registrada - Ação: {}, Entidade: {}, ID: {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Erro ao salvar auditoria - Ação: {}, Erro: {}", action, e.getMessage(), e);
        }
    }

    @Override
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
            log.debug("Auditoria simples registrada - Ação: {}", action);
        } catch (Exception e) {
            log.error("Erro ao salvar auditoria simples - Ação: {}, Erro: {}", action, e.getMessage(), e);
        }
    }

    @Override
    public Page<AuditLogResponseDTO> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> findByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> findByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::toResponseDTO);
    }

    private AuditLogResponseDTO toResponseDTO(AuditLog auditLog) {
        return AuditLogResponseDTO.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .userId(auditLog.getUserId())
                .userEmail(auditLog.getUserEmail())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .details(auditLog.getDetails())
                .timestamp(auditLog.getTimestamp())
                .build();
    }

    public void recordAction(String email, String action) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserEmail(email);
            auditLog.setAction(action);
            auditLog.setEntityType("AUTH");
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
            log.info("Auditoria - Ação registrada: {} para usuário: {}", action, email);
        } catch (Exception e) {
            log.error("Erro ao salvar auditoria de ação: {}", e.getMessage(), e);
        }
    }
}