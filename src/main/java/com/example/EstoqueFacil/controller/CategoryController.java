package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.category.CategoryRequestDTO;
import com.example.EstoqueFacil.dto.category.CategoryResponseDTO;
import com.example.EstoqueFacil.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias de produtos")
@SecurityRequirement(name = "bearer-auth")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Criar nova categoria",
            description = "Cria uma categoria de produto no sistema.\n\n**Regras:** Apenas usuários com role ADMIN podem criar categorias.\n\n**Exemplo de requisição:**\n```json\n{\n  \"name\": \"Eletrônicos\",\n  \"description\": \"Produtos eletrônicos em geral\"\n}\n```"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso", content = @Content(schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado - Necessário role ADMIN", content = @Content)
    })
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        log.info("Categoria - Criando: {}", requestDTO.getName());
        CategoryResponseDTO response = categoryService.create(requestDTO);
        log.info("Categoria - Criada com sucesso. ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza os dados de uma categoria existente. Apenas ADMIN pode atualizar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {

        log.info("Categoria - Atualizando ID: {}", id);
        CategoryResponseDTO response = categoryService.update(id, requestDTO);
        log.info("Categoria - Atualizada com sucesso. ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna os detalhes de uma categoria específica pelo seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    public ResponseEntity<CategoryResponseDTO> findById(@PathVariable @Min(1) Long id) {
        log.info("Categoria - Busca por ID: {}", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @Operation(
            summary = "Listar categorias ativas",
            description = "Retorna todas as categorias ativas do sistema, ordenadas por nome."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    })
    public ResponseEntity<List<CategoryResponseDTO>> findAllActive() {
        log.info("Categoria - Listando todas ativas");
        return ResponseEntity.ok(categoryService.findAllActive());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desativar categoria",
            description = "Realiza desativação lógica da categoria (soft delete). A categoria não é removida do banco, apenas marcada como inativa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso (sem conteúdo)"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<Void> deactivate(@PathVariable @Min(1) Long id) {
        log.warn("Categoria - Desativando ID: {}", id);
        categoryService.deactivate(id);
        log.info("Categoria - Desativada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}