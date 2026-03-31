package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductFilterDTO;
import com.example.EstoqueFacil.dto.product.ProductRequestDTO;
import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.ProductMapper;
import com.example.EstoqueFacil.repository.CategoryRepository;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductBatchRepository productBatchRepository;  // ✅ Adicionado para estoque

    @Override
    public ProductResponseDTO create(ProductRequestDTO requestDTO) {
        if (productRepository.existsByBarcode(requestDTO.getBarcode())) {
            throw new BusinessException("Código de barras já existe: " + requestDTO.getBarcode());
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + requestDTO.getCategoryId()));

        Product product = productMapper.toEntity(requestDTO);
        product.setCategory(category);
        product.setActive(true);

        Product saved = productRepository.save(product);
        return productMapper.toResponseDTO(saved);
    }

    @Override
    public ProductResponseDTO update(Long id, ProductUpdateDTO updateDTO) {
        Product product = findByIdEntity(id);
        productMapper.updateEntity(product, updateDTO);
        Product updated = productRepository.save(product);
        return productMapper.toResponseDTO(updated);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        Product product = findByIdEntity(id);
        return productMapper.toResponseDTO(product);
    }

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> searchByName(String name, Pageable pageable) {
        return productRepository.searchByName(name, pageable)
                .map(productMapper::toResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toResponseDTO);
    }

    @Override
    public ProductResponseDTO findByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com código: " + barcode));
        return productMapper.toResponseDTO(product);
    }

    @Override
    public void deactivate(Long id) {
        Product product = findByIdEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // ✅ IMPLEMENTAÇÃO DO MÉTODO FILTER
    @Override
    public Page<ProductResponseDTO> filter(ProductFilterDTO filter, Pageable pageable) {
        // Busca os produtos usando Specifications
        Page<Product> products = productRepository.findAll(
                ProductSpecification.withFilters(filter),
                pageable
        );

        // Converte para DTO e adiciona informações de estoque
        return products.map(product -> {
            ProductResponseDTO dto = productMapper.toResponseDTO(product);
            // Adiciona estoque atual ao DTO
            Integer currentStock = productBatchRepository.getTotalStockByProduct(product.getId());
            dto.setCurrentStock(currentStock);
            // Recalcula status de estoque
            dto.setStockStatus(calculateStockStatus(currentStock, product.getMinimumStock()));
            return dto;
        });
    }

    private Product findByIdEntity(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + id));
    }

    private String calculateStockStatus(Integer currentStock, Integer minimumStock) {
        if (currentStock == null || minimumStock == null) {
            return "INDISPONÍVEL";
        }
        if (currentStock >= minimumStock) {
            return "OK";
        } else if (currentStock >= minimumStock * 0.5) {
            return "BAIXO";
        } else {
            return "CRÍTICO";
        }
    }
}