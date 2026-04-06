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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductBatchRepository productBatchRepository;

    @Override
    public ProductResponseDTO create(ProductRequestDTO requestDTO) {
        if (productRepository.existsByBarcode(requestDTO.getBarcode())) {
            log.warn("Produto - Tentativa de criar com código de barras duplicado: {}", requestDTO.getBarcode());
            throw new BusinessException("Código de barras já existe: " + requestDTO.getBarcode());
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Produto - Categoria não encontrada. ID: {}", requestDTO.getCategoryId());
                    return new ResourceNotFoundException("Categoria não encontrada com ID: " + requestDTO.getCategoryId());
                });

        Product product = productMapper.toEntity(requestDTO);
        product.setCategory(category);
        product.setActive(true);

        Product saved = productRepository.save(product);
        log.info("Produto - Criado com sucesso. ID: {}, Nome: {}, Categoria: {}", saved.getId(), saved.getName(), category.getName());
        return productMapper.toResponseDTO(saved);
    }

    @Override
    public ProductResponseDTO update(Long id, ProductUpdateDTO updateDTO) {
        Product product = findByIdEntity(id);
        productMapper.updateEntity(product, updateDTO);
        Product updated = productRepository.save(product);
        log.info("Produto - Atualizado com sucesso. ID: {}, Nome: {}", updated.getId(), updated.getName());
        return productMapper.toResponseDTO(updated);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        Product product = findByIdEntity(id);
        Integer currentStock = productBatchRepository.getTotalStockByProduct(id);
        if (currentStock < product.getMinimumStock()) {
            log.warn("Produto - Estoque abaixo do mínimo. ID: {}, Atual: {}, Mínimo: {}", id, currentStock, product.getMinimumStock());
        }
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
                .orElseThrow(() -> {
                    log.warn("Produto - Não encontrado por código de barras: {}", barcode);
                    return new ResourceNotFoundException("Produto não encontrado com código: " + barcode);
                });
        return productMapper.toResponseDTO(product);
    }

    @Override
    public void deactivate(Long id) {
        Product product = findByIdEntity(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Produto - Desativado com sucesso. ID: {}, Nome: {}", id, product.getName());
    }

    @Override
    public Page<ProductResponseDTO> filter(ProductFilterDTO filter, Pageable pageable) {
        Page<Product> products = productRepository.findAll(ProductSpecification.withFilters(filter), pageable);
        return products.map(product -> {
            ProductResponseDTO dto = productMapper.toResponseDTO(product);
            Integer currentStock = productBatchRepository.getTotalStockByProduct(product.getId());
            dto.setCurrentStock(currentStock);
            dto.setStockStatus(calculateStockStatus(currentStock, product.getMinimumStock()));
            return dto;
        });
    }

    private Product findByIdEntity(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> {
                    log.warn("Produto - Não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Produto não encontrado com ID: " + id);
                });
    }

    private String calculateStockStatus(Integer currentStock, Integer minimumStock) {
        if (currentStock == null || minimumStock == null) return "INDISPONÍVEL";
        if (currentStock >= minimumStock) return "OK";
        if (currentStock >= minimumStock * 0.5) return "BAIXO";
        return "CRÍTICO";
    }
}