package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import com.example.EstoqueFacil.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Endpoints para consulta de logs de auditoria")
@SecurityRequirement(name = "bearer-auth")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDTO>> findAll(
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Listando todos os logs de auditoria");
        return ResponseEntity.ok(auditService.findAll(pageable));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByEntity(
            @PathVariable @NotBlank String entityType,
            @PathVariable @Min(1) Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Buscando logs por entidade: {} - ID: {}", entityType, entityId);
        return ResponseEntity.ok(auditService.findByEntity(entityType, entityId, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByUser(
            @PathVariable @Min(1) Long userId,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Buscando logs do usuário ID: {}", userId);
        return ResponseEntity.ok(auditService.findByUser(userId, pageable));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByAction(
            @PathVariable @NotBlank String action,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Buscando logs por ação: {}", action);
        return ResponseEntity.ok(auditService.findByAction(action, pageable));
    }
}