package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.dto.stock.StockMovementResponseDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Endpoints para movimentação de estoque")
//@SecurityRequirement(name = "bearer-auth")
public class StockController {

    private final StockService stockService;

    @PostMapping("/entry")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Registrar entrada de produtos (compra)")
    public ResponseEntity<Void> registerEntry(@Valid @RequestBody StockEntryDTO entryDTO) {
        log.info("Registrando ENTRADA de estoque - Produto ID: {}, Quantidade: {}, Usuário ID: {}",
                entryDTO.getProductId(),
                entryDTO.getQuantity(),
                entryDTO.getUserId());

        long startTime = System.currentTimeMillis();
        stockService.registerEntry(entryDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("ENTRADA registrada com sucesso em {}ms. Produto ID: {}, Quantidade: {}",
                duration, entryDTO.getProductId(), entryDTO.getQuantity());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/exit")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Registrar saída de produtos (venda ou perda)")
    public ResponseEntity<Void> registerExit(@Valid @RequestBody StockExitDTO exitDTO) {
        log.info("Registrando SAÍDA de estoque - Produto ID: {}, Quantidade: {}, Tipo: {}, Usuário ID: {}",
                exitDTO.getProductId(),
                exitDTO.getQuantity(),
                exitDTO.getType(),
                exitDTO.getUserId());

        long startTime = System.currentTimeMillis();
        stockService.registerExit(exitDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("SAÍDA registrada com sucesso em {}ms. Produto ID: {}, Quantidade: {}, Tipo: {}",
                duration, exitDTO.getProductId(), exitDTO.getQuantity(), exitDTO.getType());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/movements")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar histórico de movimentações")
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovements(
            @PageableDefault(size = 50, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Listando histórico de movimentações - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        long startTime = System.currentTimeMillis();
        Page<StockMovementResponseDTO> response = stockService.getMovements(pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Histórico de movimentações retornado em {}ms. Total de registros: {}, Total de páginas: {}",
                duration, response.getTotalElements(), response.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements/product/{productId}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar movimentações de um produto específico")
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovementsByProduct(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long productId,
            @PageableDefault(size = 50, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Listando movimentações do produto ID: {} - Página: {}, Tamanho: {}",
                productId, pageable.getPageNumber(), pageable.getPageSize());

        // ✅ VALIDAÇÃO: ID não pode ser nulo ou negativo (já validado por @Min)

        long startTime = System.currentTimeMillis();
        Page<StockMovementResponseDTO> response = stockService.getMovementsByProduct(productId, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Movimentações do produto ID: {} retornadas em {}ms. Total de registros: {}",
                productId, duration, response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current/{productId}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Consultar estoque atual de um produto")
    public ResponseEntity<Integer> getCurrentStock(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long productId) {

        log.info("Consultando estoque atual do produto ID: {}", productId);

        long startTime = System.currentTimeMillis();
        Integer stock = stockService.getCurrentStock(productId);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque atual do produto ID: {} é {} unidades. Consulta realizada em {}ms",
                productId, stock, duration);

        // ✅ ALERTA: estoque baixo
        if (stock != null && stock < 10) {
            log.warn("Produto ID: {} com estoque baixo: {} unidades", productId, stock);
        }

        // ✅ ALERTA: estoque zerado
        if (stock != null && stock == 0) {
            log.warn("Produto ID: {} com estoque ZERADO!", productId);
        }

        return ResponseEntity.ok(stock);
    }
}