package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.common.PageableResponse;
import com.example.EstoqueFacil.dto.product.ProductFilterDTO;
import com.example.EstoqueFacil.dto.product.ProductRequestDTO;
import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
@SecurityRequirement(name = "bearer-auth")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo produto", description = "Apenas ADMIN pode criar produtos")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO requestDTO) {
        log.info("ADMIN criando novo produto - Nome: {}, Código de barras: {}, Preço venda: {}",
                requestDTO.getName(), requestDTO.getBarcode(), requestDTO.getSalePrice());

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.create(requestDTO);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto criado com sucesso em {}ms. ID: {}, Nome: {}", duration, response.getId(), response.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id,
            @Valid @RequestBody ProductUpdateDTO dto) {

        log.info("ADMIN atualizando produto ID: {} - Nome: {}", id, dto.getName());

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.update(id, dto);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto ID: {} atualizado com sucesso em {}ms", id, duration);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponseDTO> findById(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id) {

        log.info("Buscando produto por ID: {}", id);

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.findById(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto encontrado em {}ms. ID: {}, Nome: {}, Estoque atual: {}",
                duration, response.getId(), response.getName(), response.getCurrentStock());

        // ✅ ALERTA: produto com estoque baixo
        if ("BAIXO".equals(response.getStockStatus()) || "CRÍTICO".equals(response.getStockStatus())) {
            log.warn("Produto ID: {} com estoque {}: {} unidades (mínimo: {})",
                    response.getId(), response.getStockStatus(), response.getCurrentStock(), response.getMinimumStock());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar todos os produtos (paginado)")
    public ResponseEntity<PageableResponse<ProductResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Listando produtos - Página: {}, Tamanho: {}, Ordenação: {} ({})",
                page, size, sortBy, direction);

        // ✅ VALIDAÇÃO: campos permitidos para ordenação
        String[] allowedSortFields = {"name", "price", "createdAt", "barcode"};
        if (!java.util.Arrays.asList(allowedSortFields).contains(sortBy)) {
            log.warn("Tentativa de ordenação por campo não permitido: {}", sortBy);
            throw new BusinessException("Campo de ordenação não permitido. Use: name, price, createdAt, barcode");
        }

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> pageResult = productService.findAll(pageable);
        long duration = System.currentTimeMillis() - startTime;

        PageableResponse<ProductResponseDTO> response = PageableResponse.fromPage(pageResult);

        log.info("Listagem de produtos concluída em {}ms. Total de produtos: {}, Total de páginas: {}",
                duration, pageResult.getTotalElements(), pageResult.getTotalPages());

        // ✅ HEADERS de paginação
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
                .header("X-Page", String.valueOf(pageResult.getNumber()))
                .header("X-Page-Size", String.valueOf(pageResult.getSize()))
                .header("X-Total-Pages", String.valueOf(pageResult.getTotalPages()))
                .body(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Buscar produtos por nome")
    public ResponseEntity<Page<ProductResponseDTO>> searchByName(
            @RequestParam @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("Buscando produtos por nome: '{}' - Página: {}, Tamanho: {}", name, pageable.getPageNumber(), pageable.getPageSize());

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> response = productService.searchByName(name, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Busca por nome concluída em {}ms. Resultados encontrados: {}", duration, response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Listar produtos por categoria")
    public ResponseEntity<Page<ProductResponseDTO>> findByCategory(
            @PathVariable @Min(value = 1, message = "ID da categoria deve ser maior que 0") Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("Listando produtos da categoria ID: {} - Página: {}, Tamanho: {}", categoryId, pageable.getPageNumber(), pageable.getPageSize());

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> response = productService.findByCategory(categoryId, pageable);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produtos da categoria ID: {} retornados em {}ms. Total: {}", categoryId, duration, response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Buscar produto por código de barras")
    public ResponseEntity<ProductResponseDTO> findByBarcode(
            @PathVariable @NotBlank(message = "Código de barras não pode ser vazio")
            @Pattern(regexp = "^[0-9]{8,14}$", message = "Código de barras deve ter entre 8 e 14 dígitos numéricos")
            String barcode) {

        log.info("Buscando produto por código de barras: {}", barcode);

        long startTime = System.currentTimeMillis();
        ProductResponseDTO response = productService.findByBarcode(barcode);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto encontrado em {}ms. ID: {}, Nome: {}", duration, response.getId(), response.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Buscar produtos com filtros avançados")
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

        log.info("Filtrando produtos - Nome: {}, Código: {}, Categoria: {}, Preço: {} a {}, Status: {}, Ativo: {}",
                name, barcode, categoryId, minPrice, maxPrice, stockStatus, active);

        // ✅ VALIDAÇÃO: preço mínimo não pode ser maior que máximo
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            log.warn("Preço mínimo ({}) maior que preço máximo ({})", minPrice, maxPrice);
            throw new BusinessException("Preço mínimo não pode ser maior que preço máximo");
        }

        // ✅ VALIDAÇÃO: campos permitidos para ordenação
        String[] allowedSortFields = {"name", "salePrice", "costPrice", "createdAt", "minimumStock"};
        if (!java.util.Arrays.asList(allowedSortFields).contains(sortBy)) {
            log.warn("Tentativa de ordenação por campo não permitido: {}", sortBy);
            throw new BusinessException("Campo de ordenação não permitido. Use: name, salePrice, costPrice, createdAt, minimumStock");
        }

        // ✅ VALIDAÇÃO: status de estoque permitido
        if (stockStatus != null && !stockStatus.matches("^(OK|BAIXO|CRÍTICO)$")) {
            log.warn("Status de estoque inválido: {}", stockStatus);
            throw new BusinessException("Status de estoque deve ser OK, BAIXO ou CRÍTICO");
        }

        ProductFilterDTO filter = ProductFilterDTO.builder()
                .name(name)
                .barcode(barcode)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .stockStatus(stockStatus)
                .active(active)
                .build();

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        long startTime = System.currentTimeMillis();
        Page<ProductResponseDTO> pageResult = productService.filter(filter, pageable);
        long duration = System.currentTimeMillis() - startTime;

        PageableResponse<ProductResponseDTO> response = PageableResponse.fromPage(pageResult);

        log.info("Filtro concluído em {}ms. Resultados: {}", duration, pageResult.getTotalElements());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
                .header("X-Page", String.valueOf(pageResult.getNumber()))
                .header("X-Page-Size", String.valueOf(pageResult.getSize()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar produto (soft delete)")
    public ResponseEntity<Void> deactivate(
            @PathVariable @Min(value = 1, message = "ID do produto deve ser maior que 0") Long id) {

        log.warn("ADMIN desativando produto ID: {}", id);

        long startTime = System.currentTimeMillis();
        productService.deactivate(id);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Produto ID: {} desativado com sucesso em {}ms", id, duration);

        return ResponseEntity.noContent().build();
    }

}