package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.common.PageableResponse;
import com.example.EstoqueFacil.dto.product.ProductFilterDTO;
import com.example.EstoqueFacil.dto.product.ProductRequestDTO;
import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos do estoque")
@SecurityRequirement(name = "bearer-auth")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar novo produto",
            description = "Cria um novo produto no sistema.\n\n**Regras de negócio:**\n" +
                    "- Apenas ADMIN pode criar produtos\n" +
                    "- Código de barras deve ser único\n" +
                    "- Preço de venda deve ser maior que preço de custo\n" +
                    "- Estoque mínimo não pode ser negativo\n\n" +
                    "**Exemplo de requisição:**\n```json\n{\n  \"name\": \"Smartphone\",\n  \"barcode\": \"7891234567890\",\n  \"costPrice\": 800.00,\n  \"salePrice\": 1200.00,\n  \"minimumStock\": 10,\n  \"categoryId\": 1\n}\n```"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (código duplicado, preços inválidos)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Necessário role ADMIN", content = @Content)
    })
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO requestDTO) {
        log.info("Produto - ADMIN criando. Nome: {}, Código: {}, Preço venda: {}",
                requestDTO.getName(), requestDTO.getBarcode(), requestDTO.getSalePrice());

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.create(requestDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Criado com sucesso. ID: {}, Nome: {}, Tempo: {}ms", response.getId(), response.getName(), duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza os dados de um produto existente. Apenas ADMIN pode atualizar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id,
            @Valid @RequestBody ProductUpdateDTO dto) {

        log.info("Produto - ADMIN atualizando. ID: {}, Nome: {}", id, dto.getName());

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.update(id, dto);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Atualizado com sucesso. ID: {}, Tempo: {}ms", id, duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna os detalhes de um produto específico, incluindo estoque atual e status crítico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id) {
        log.info("Produto - Busca por ID: {}", id);

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.findById(id);
        long duration = System.currentTimeMillis() - startTime;

        if ("BAIXO".equals(response.getStockStatus()) || "CRÍTICO".equals(response.getStockStatus())) {
            log.warn("Produto - Estoque crítico. ID: {}, Status: {}, Estoque: {}/{}",
                    response.getId(), response.getStockStatus(), response.getCurrentStock(), response.getMinimumStock());
        }

        log.info("Produto - Encontrado. ID: {}, Nome: {}, Tempo: {}ms", response.getId(), response.getName(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Listar produtos (paginado)",
            description = "Retorna uma lista paginada de todos os produtos ativos.\n\n" +
                    "**Parâmetros de paginação:**\n" +
                    "- `page`: número da página (padrão: 0)\n" +
                    "- `size`: itens por página (padrão: 20, máximo: 100)\n" +
                    "- `sortBy`: campo para ordenação (name, price, createdAt, barcode)\n" +
                    "- `direction`: asc ou desc"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    })
    public ResponseEntity<PageableResponse<ProductResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        String[] allowedSortFields = {"name", "price", "createdAt", "barcode"};
        if (!java.util.Arrays.asList(allowedSortFields).contains(sortBy)) {
            log.warn("Produto - Tentativa de ordenação inválida: {}", sortBy);
            throw new BusinessException("Campo de ordenação não permitido. Use: name, price, createdAt, barcode");
        }

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> pageResult = productService.findAll(pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Listagem concluída. Total: {}, Páginas: {}, Tempo: {}ms",
                pageResult.getTotalElements(), pageResult.getTotalPages(), duration);

        PageableResponse<ProductResponseDTO> response = PageableResponse.fromPage(pageResult);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
                .header("X-Page", String.valueOf(pageResult.getNumber()))
                .header("X-Page-Size", String.valueOf(pageResult.getSize()))
                .header("X-Total-Pages", String.valueOf(pageResult.getTotalPages()))
                .body(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Buscar produtos por nome",
            description = "Realiza busca parcial pelo nome do produto (case insensitive)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome deve ter entre 2 e 100 caracteres", content = @Content)
    })
    public ResponseEntity<Page<ProductResponseDTO>> searchByName(
            @RequestParam @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("Produto - Busca por nome: '{}'", name);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> response = productService.searchByName(name, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Busca concluída. Resultados: {}, Tempo: {}ms", response.getTotalElements(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Listar produtos por categoria",
            description = "Retorna todos os produtos pertencentes a uma categoria específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    public ResponseEntity<Page<ProductResponseDTO>> findByCategory(
            @PathVariable @Min(value = 1, message = "ID da categoria deve ser maior que 0") Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("Produto - Listando por categoria ID: {}", categoryId);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> response = productService.findByCategory(categoryId, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Listagem por categoria concluída. Total: {}, Tempo: {}ms", response.getTotalElements(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Buscar produto por código de barras",
            description = "Busca um produto específico pelo seu código de barras único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Código de barras inválido (8-14 dígitos)", content = @Content)
    })
    public ResponseEntity<ProductResponseDTO> findByBarcode(
            @PathVariable @NotBlank(message = "Código de barras não pode ser vazio")
            @Pattern(regexp = "^[0-9]{8,14}$", message = "Código de barras deve ter entre 8 e 14 dígitos numéricos")
            String barcode) {

        log.info("Produto - Busca por código de barras: {}", barcode);

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.findByBarcode(barcode);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Encontrado por código. ID: {}, Nome: {}, Tempo: {}ms", response.getId(), response.getName(), duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Buscar produtos com filtros avançados",
            description = "Busca produtos utilizando múltiplos critérios de filtro simultaneamente.\n\n" +
                    "**Filtros disponíveis:**\n" +
                    "- `name`: nome do produto (busca parcial)\n" +
                    "- `barcode`: código de barras\n" +
                    "- `categoryId`: ID da categoria\n" +
                    "- `minPrice`: preço mínimo\n" +
                    "- `maxPrice`: preço máximo\n" +
                    "- `stockStatus`: OK, BAIXO ou CRÍTICO\n" +
                    "- `active`: ativo/inativo\n\n" +
                    "**Regras:**\n" +
                    "- Preço mínimo não pode ser maior que preço máximo\n" +
                    "- Ordenação permitida apenas nos campos: name, salePrice, costPrice, createdAt, minimumStock"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos", content = @Content)
    })
    public ResponseEntity<PageableResponse<ProductResponseDTO>> filterProducts(
            @RequestParam(required = false) @Size(min = 2, max = 100) String name,
            @RequestParam(required = false) @Pattern(regexp = "^[0-9]{8,14}$") String barcode,
            @RequestParam(required = false) @Min(1) Long categoryId,
            @RequestParam(required = false) @DecimalMin("0.01") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin("0.01") BigDecimal maxPrice,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Produto - Filtro avançado. Nome: {}, Código: {}, Categoria: {}, Preço: {} a {}, Status: {}, Ativo: {}",
                name, barcode, categoryId, minPrice, maxPrice, stockStatus, active);

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            log.warn("Produto - Filtro inválido: preço mínimo > máximo. Mín: {}, Máx: {}", minPrice, maxPrice);
            throw new BusinessException("Preço mínimo não pode ser maior que preço máximo");
        }

        String[] allowedSortFields = {"name", "salePrice", "costPrice", "createdAt", "minimumStock"};
        if (!java.util.Arrays.asList(allowedSortFields).contains(sortBy)) {
            log.warn("Produto - Tentativa de ordenação inválida no filtro: {}", sortBy);
            throw new BusinessException("Campo de ordenação não permitido. Use: name, salePrice, costPrice, createdAt, minimumStock");
        }

        if (stockStatus != null && !stockStatus.matches("^(OK|BAIXO|CRÍTICO)$")) {
            log.warn("Produto - Status de estoque inválido: {}", stockStatus);
            throw new BusinessException("Status de estoque deve ser OK, BAIXO ou CRÍTICO");
        }

        ProductFilterDTO filter = ProductFilterDTO.builder()
                .name(name).barcode(barcode).categoryId(categoryId)
                .minPrice(minPrice).maxPrice(maxPrice).stockStatus(stockStatus).active(active).build();

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> pageResult = productService.filter(filter, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Filtro concluído. Resultados: {}, Tempo: {}ms", pageResult.getTotalElements(), duration);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
                .header("X-Page", String.valueOf(pageResult.getNumber()))
                .header("X-Page-Size", String.valueOf(pageResult.getSize()))
                .body(PageableResponse.fromPage(pageResult));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar produto",
            description = "Realiza desativação lógica do produto (soft delete). O produto não é removido do banco, apenas marcado como inativo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Void> deactivate(@PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id) {
        log.warn("Produto - ADMIN desativando ID: {}", id);

        long startTime = System.currentTimeMillis();
        productService.deactivate(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto - Desativado com sucesso. ID: {}, Tempo: {}ms", id, duration);
        return ResponseEntity.noContent().build();
    }
}