package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import com.example.EstoqueFacil.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Endpoints para consulta de logs de auditoria")
@SecurityRequirement(name = "bearer-auth")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os logs de auditoria", description = "Apenas ADMIN pode ver logs")
    public ResponseEntity<Page<AuditLogResponseDTO>> findAll(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AuditLogResponseDTO> response = auditService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar logs por entidade")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AuditLogResponseDTO> response = auditService.findByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar logs por usuário")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AuditLogResponseDTO> response = auditService.findByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar logs por ação")
    public ResponseEntity<Page<AuditLogResponseDTO>> findByAction(
            @PathVariable String action,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AuditLogResponseDTO> response = auditService.findByAction(action, pageable);
        return ResponseEntity.ok(response);
    }
}