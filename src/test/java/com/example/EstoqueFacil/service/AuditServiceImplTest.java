package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import com.example.EstoqueFacil.entity.AuditLog;
import com.example.EstoqueFacil.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setAction("CREATE");
        auditLog.setEntityType("PRODUCT");
        auditLog.setEntityId(1L);
        auditLog.setUserId(1L);
        auditLog.setUserEmail("admin@teste.com");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria com sucesso")
    void shouldLogAuditSuccessfully() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditService.log("CREATE", "PRODUCT", 1L, 1L, "admin@teste.com", null, null);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve registrar log simples com sucesso")
    void shouldLogSimpleAuditSuccessfully() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditService.logSimple("DELETE", "PRODUCT", 1L, 1L, "admin@teste.com", "Produto removido");

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve buscar todos os logs paginados")
    void shouldFindAllLogs() {
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AuditLogResponseDTO> result = auditService.findAll(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(auditLogRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve buscar logs por entidade")
    void shouldFindLogsByEntity() {
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findByEntityTypeAndEntityId("PRODUCT", 1L, PageRequest.of(0, 10)))
                .thenReturn(page);

        Page<AuditLogResponseDTO> result = auditService.findByEntity("PRODUCT", 1L, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(auditLogRepository, times(1)).findByEntityTypeAndEntityId("PRODUCT", 1L, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve buscar logs por usuário")
    void shouldFindLogsByUser() {
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findByUserId(1L, PageRequest.of(0, 10))).thenReturn(page);

        Page<AuditLogResponseDTO> result = auditService.findByUser(1L, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(auditLogRepository, times(1)).findByUserId(1L, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve buscar logs por ação")
    void shouldFindLogsByAction() {
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findByAction("CREATE", PageRequest.of(0, 10))).thenReturn(page);

        Page<AuditLogResponseDTO> result = auditService.findByAction("CREATE", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(auditLogRepository, times(1)).findByAction("CREATE", PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Deve registrar ação de autenticação")
    void shouldRecordAuthAction() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        auditService.recordAction("admin@teste.com", "LOGIN_SUCCESS");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog captured = captor.getValue();
        assertThat(captured.getUserEmail()).isEqualTo("admin@teste.com");
        assertThat(captured.getAction()).isEqualTo("LOGIN_SUCCESS");
        assertThat(captured.getEntityType()).isEqualTo("AUTH");
    }
}