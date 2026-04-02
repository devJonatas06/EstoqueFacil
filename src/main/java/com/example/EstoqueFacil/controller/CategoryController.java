package com.example.EstoqueFacil.controller;

import com.example.EstoqueFacil.dto.category.CategoryRequestDTO;
import com.example.EstoqueFacil.dto.category.CategoryResponseDTO;
import com.example.EstoqueFacil.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
//@SecurityRequirement(name = "bearer-auth")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> create(
            @Valid @RequestBody CategoryRequestDTO requestDTO) {

        log.info("Criando categoria: {}", requestDTO.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(requestDTO));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {

        log.info("Atualizando categoria ID: {}", id);
        return ResponseEntity.ok(categoryService.update(id, requestDTO));
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<CategoryResponseDTO> findById(
            @PathVariable @Min(1) Long id) {

        log.info("Buscando categoria ID: {}", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ResponseEntity<List<CategoryResponseDTO>> findAllActive() {

        log.info("Listando categorias ativas");
        return ResponseEntity.ok(categoryService.findAllActive());
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable @Min(1) Long id) {

        log.warn("Desativando categoria ID: {}", id);
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}