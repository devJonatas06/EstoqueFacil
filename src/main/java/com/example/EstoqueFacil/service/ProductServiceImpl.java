package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.Category;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
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

    @Override
    public Product create(Product product) {

        if (productRepository.existsByBarcode(product.getBarcode())) {
            throw new BusinessException("Código de barras já existe");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        product.setCategory(category);
        product.setActive(true);

        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product updated) {

        Product product = findById(id);

        product.setName(updated.getName());
        product.setMaker(updated.getMaker());
        product.setDescription(updated.getDescription());
        product.setSalePrice(updated.getSalePrice());
        product.setCostPrice(updated.getCostPrice());
        product.setMinimumStock(updated.getMinimumStock());

        return productRepository.save(product);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public void deactivate(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }
}