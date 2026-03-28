package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.entity.*;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;


    @Override
    public void registerEntry(Long productId, Integer quantity, LocalDate expirationDate, Long userId, String observation) {

        validateQuantity(quantity);

        Product product = getProductOrThrow(productId);
        User user = getUserOrThrow(userId);

        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setQuantity(quantity);
        batch.setExpirationDate(expirationDate);
        batch.setActive(true);

        productBatchRepository.save(batch);

        createMovement(batch, quantity, StockMovementType.ENTRY, user, observation);
    }


    @Override
    public void registerExit(Long productId, Integer quantity, Long userId, String observation, StockMovementType type) {

        validateQuantity(quantity);
        validateExitType(type);

        Product product = getProductOrThrow(productId);
        User user = getUserOrThrow(userId);

        Integer totalStock = productBatchRepository.getTotalStockByProduct(productId);

        if (totalStock < quantity) {
            throw new BusinessException("Estoque insuficiente");
        }

        List<ProductBatch> batches = productBatchRepository.findByActiveTrueOrderByExpirationDate();

        int remaining = quantity;

        for (ProductBatch batch : batches) {

            if (!batch.getProduct().getId().equals(productId)) continue;

            if (remaining <= 0) break;

            int available = batch.getQuantity();
            if (available <= 0) continue;

            int toRemove = Math.min(available, remaining);

            batch.setQuantity(available - toRemove);

            if (batch.getQuantity() == 0) {
                batch.setActive(false);
            }

            productBatchRepository.save(batch);

            createMovement(batch, toRemove, type, user, observation);

            remaining -= toRemove;
        }
    }


    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findBelowMinimumStock();
    }


    private Product getProductOrThrow(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.isActive()) {
            throw new BusinessException("Produto inativo");
        }

        return product;
    }

    private User getUserOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new BusinessException("Usuário inativo");
        }

        return user;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
    }

    private void validateExitType(StockMovementType type) {
        if (type != StockMovementType.SALE && type != StockMovementType.LOSS) {
            throw new BusinessException("Tipo inválido para saída");
        }
    }

    private void createMovement(ProductBatch batch, Integer quantity, StockMovementType type, User user, String observation) {

        StockMovement movement = new StockMovement();
        movement.setBatch(batch);
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setUser(user);
        movement.setObservation(observation);

        stockMovementRepository.save(movement);
    }
}