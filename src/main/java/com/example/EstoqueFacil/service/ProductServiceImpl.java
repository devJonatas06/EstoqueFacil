package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.product.ProductResponseDTO;
import com.example.EstoqueFacil.dto.product.ProductUpdateDTO;
import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.ProductMapper;
import com.example.EstoqueFacil.repository.CategoryRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO create(Product product) {

        if (productRepository.existsByBarcode(product.getBarcode())) {
            throw new BusinessException("Código de barras já existe");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        product.setCategory(category);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        
        // ✅ Retorna DTO, não Entity!
        return productMapper.toResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO update(Long id, ProductUpdateDTO updateDTO) {

        Product product = findByIdEntity(id);
        
        productMapper.updateEntity(product, updateDTO);

        Product updatedProduct = productRepository.save(product);
        
        // ✅ Retorna DTO, não Entity!
        return productMapper.toResponseDTO(updatedProduct);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        Product product = findByIdEntity(id);
        // ✅ Retorna DTO, não Entity!
        return productMapper.toResponseDTO(product);
    }

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        // ✅ Retorna Page de DTO, não Entity!
        return products.map(productMapper::toResponseDTO);
    }

    @Override
    public void deactivate(Long id) {
        Product product = findByIdEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // Método auxiliar interno - retorna Entity para operações internas
    private Product findByIdEntity(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }
}