package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.dto.stock.StockMovementResponseDTO;
import com.example.EstoqueFacil.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Endpoints para movimentação de estoque")
@SecurityRequirement(name = "bearer-auth")
public class StockController {

    private final StockService stockService;

    @PostMapping("/entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Registrar entrada de produtos (compra)")
    public ResponseEntity<Void> registerEntry(@Valid @RequestBody StockEntryDTO entryDTO) {
        stockService.registerEntry(entryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Registrar saída de produtos (venda ou perda)")
    public ResponseEntity<Void> registerExit(@Valid @RequestBody StockExitDTO exitDTO) {
        stockService.registerExit(exitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar histórico de movimentações")
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovements(
            @PageableDefault(size = 50) Pageable pageable) {
        Page<StockMovementResponseDTO> response = stockService.getMovements(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar movimentações de um produto específico")
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovementsByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<StockMovementResponseDTO> response = stockService.getMovementsByProduct(productId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Consultar estoque atual de um produto")
    public ResponseEntity<Integer> getCurrentStock(@PathVariable Long productId) {
        Integer stock = stockService.getCurrentStock(productId);
        return ResponseEntity.ok(stock);
    }
}