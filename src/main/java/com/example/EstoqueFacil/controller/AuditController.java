package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.report.AuditLogResponseDTO;
import com.example.EstoqueFacil.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Endpoints para consulta de logs de auditoria - Acompanhamento de todas as ações do sistema")
@SecurityRequirement(name = "bearer-auth")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todos os logs de auditoria",
            description = "Retorna uma lista paginada de todos os logs de auditoria do sistema. Apenas usuários com role ADMIN podem acessar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não tem permissão de ADMIN", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    public ResponseEntity<Page<AuditLogResponseDTO>> findAll(
            @PageableDefault(size = 50) Pageable pageable) {

        Page<AuditLogResponseDTO> result = auditService.findAll(pageable);
        log.info("Auditoria - Listagem realizada. Total de registros: {}", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar logs por entidade",
            description = "Retorna logs filtrados por tipo de entidade (PRODUCT, CATEGORY, USER, STOCK_MOVEMENT) e ID da entidade"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não tem permissão de ADMIN", content = @Content)
    })
    public ResponseEntity<Page<AuditLogResponseDTO>> findByEntity(
            @PathVariable @NotBlank String entityType,
            @PathVariable @Min(1) Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Auditoria - Busca por entidade. Tipo: {}, ID: {}", entityType, entityId);
        return ResponseEntity.ok(auditService.findByEntity(entityType, entityId, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar logs por usuário",
            description = "Retorna todos os logs de auditoria relacionados a um usuário específico pelo seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID do usuário inválido (deve ser maior que 0)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Page<AuditLogResponseDTO>> findByUser(
            @PathVariable @Min(1) Long userId,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Auditoria - Busca por usuário. ID: {}", userId);
        return ResponseEntity.ok(auditService.findByUser(userId, pageable));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Buscar logs por ação",
            description = "Retorna logs filtrados por tipo de ação (CREATE, UPDATE, DELETE, SALE, ENTRY, LOSS)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ação inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Page<AuditLogResponseDTO>> findByAction(
            @PathVariable @NotBlank String action,
            @PageableDefault(size = 50) Pageable pageable) {

        log.info("Auditoria - Busca por ação: {}", action);
        return ResponseEntity.ok(auditService.findByAction(action, pageable));
    }
}