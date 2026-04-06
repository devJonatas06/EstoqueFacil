package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.dto.stock.StockMovementResponseDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Estoque", description = "Endpoints para movimentação e controle de estoque")
@SecurityRequirement(name = "bearer-auth")
public class StockController {

    private final StockService stockService;

    @PostMapping("/entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Registrar entrada de produtos",
            description = "Registra a entrada de um lote de produtos no estoque (compra).\n\n" +
                    "**Regras de negócio:**\n" +
                    "- Quantidade deve ser maior que zero\n" +
                    "- Produto deve estar ativo\n" +
                    "- Usuário deve estar ativo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (quantidade zerada, produto inativo)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou usuário não encontrado", content = @Content)
    })
    public ResponseEntity<Void> registerEntry(@Valid @RequestBody StockEntryDTO entryDTO) {
        log.info("Estoque - ENTRADA. Produto ID: {}, Quantidade: {}, Usuário ID: {}",
                entryDTO.getProductId(), entryDTO.getQuantity(), entryDTO.getUserId());

        long startTime = System.currentTimeMillis();
        stockService.registerEntry(entryDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque - ENTRADA concluída. Produto ID: {}, Quantidade: {}, Tempo: {}ms",
                entryDTO.getProductId(), entryDTO.getQuantity(), duration);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/exit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Registrar saída de produtos",
            description = "Registra a saída de produtos do estoque (venda ou perda).\n\n" +
                    "**Regras de negócio:**\n" +
                    "- Tipo de saída: SALE (venda) ou LOSS (perda)\n" +
                    "- Quantidade não pode exceder o estoque disponível\n" +
                    "- Utiliza lógica FIFO (First-In-First-Out) para baixar os lotes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Saída registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto ou usuário não encontrado", content = @Content)
    })
    public ResponseEntity<Void> registerExit(@Valid @RequestBody StockExitDTO exitDTO) {
        log.info("Estoque - SAÍDA. Produto ID: {}, Quantidade: {}, Tipo: {}, Usuário ID: {}",
                exitDTO.getProductId(), exitDTO.getQuantity(), exitDTO.getType(), exitDTO.getUserId());

        long startTime = System.currentTimeMillis();
        stockService.registerExit(exitDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque - SAÍDA concluída. Produto ID: {}, Quantidade: {}, Tipo: {}, Tempo: {}ms",
                exitDTO.getProductId(), exitDTO.getQuantity(), exitDTO.getType(), duration);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Listar histórico de movimentações",
            description = "Retorna o histórico completo de todas as movimentações de estoque (entradas, vendas e perdas), paginado e ordenado por data decrescente."
    )
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovements(
            @PageableDefault(size = 50, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Estoque - Histórico solicitado. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());

        long startTime = System.currentTimeMillis();
        Page<StockMovementResponseDTO> response = stockService.getMovements(pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque - Histórico retornado. Total: {}, Páginas: {}, Tempo: {}ms",
                response.getTotalElements(), response.getTotalPages(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Listar movimentações de um produto",
            description = "Retorna o histórico de movimentações de um produto específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentações listadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<Page<StockMovementResponseDTO>> getMovementsByProduct(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long productId,
            @PageableDefault(size = 50, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Estoque - Histórico por produto. ID: {}, Página: {}, Tamanho: {}",
                productId, pageable.getPageNumber(), pageable.getPageSize());

        long startTime = System.currentTimeMillis();
        Page<StockMovementResponseDTO> response = stockService.getMovementsByProduct(productId, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque - Histórico por produto retornado. Produto ID: {}, Total: {}, Tempo: {}ms",
                productId, response.getTotalElements(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Consultar estoque atual",
            description = "Retorna a quantidade atual em estoque de um produto específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<Integer> getCurrentStock(@PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long productId) {
        log.info("Estoque - Consulta de estoque. Produto ID: {}", productId);

        long startTime = System.currentTimeMillis();
        Integer stock = stockService.getCurrentStock(productId);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Estoque - Estoque atual. Produto ID: {}, Quantidade: {}, Tempo: {}ms", productId, stock, duration);

        if (stock != null && stock < 10) {
            log.warn("Estoque - Produto com estoque baixo. ID: {}, Quantidade: {}", productId, stock);
        }

        if (stock != null && stock == 0) {
            log.warn("Estoque - Produto com estoque ZERADO. ID: {}", productId);
        }

        return ResponseEntity.ok(stock);
    }
}