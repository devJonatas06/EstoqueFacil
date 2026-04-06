package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.dto.stock.StockMovementResponseDTO;
import com.example.EstoqueFacil.entity.*;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import com.example.EstoqueFacil.mapper.StockMapper;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import com.example.EstoqueFacil.repository.ProductRepository;
import com.example.EstoqueFacil.repository.StockMovementRepository;
import com.example.EstoqueFacil.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final StockMapper stockMapper;

    @Override
    public void registerEntry(StockEntryDTO entryDTO) {
        validateQuantity(entryDTO.getQuantity());

        Product product = getProductOrThrow(entryDTO.getProductId());
        User user = getUserOrThrow(entryDTO.getUserId());

        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setQuantity(entryDTO.getQuantity());
        batch.setExpirationDate(entryDTO.getExpirationDate());
        batch.setActive(true);

        productBatchRepository.save(batch);
        createMovement(batch, entryDTO.getQuantity(), StockMovementType.ENTRY, user, entryDTO.getObservation());

        log.info("Estoque - ENTRADA registrada. Produto: {}, Quantidade: {}, Usuário: {}",
                product.getName(), entryDTO.getQuantity(), user.getEmail());
    }

    @Override
    public void registerExit(StockExitDTO exitDTO) {
        validateQuantity(exitDTO.getQuantity());
        validateExitType(exitDTO.getType());

        Product product = getProductOrThrow(exitDTO.getProductId());
        User user = getUserOrThrow(exitDTO.getUserId());

        Integer totalStock = productBatchRepository.getTotalStockByProduct(exitDTO.getProductId());

        if (totalStock < exitDTO.getQuantity()) {
            log.warn("Estoque - SAÍDA negada. Estoque insuficiente. Produto: {}, Disponível: {}, Solicitado: {}",
                    product.getName(), totalStock, exitDTO.getQuantity());
            throw new BusinessException("Estoque insuficiente. Disponível: " + totalStock);
        }

        List<ProductBatch> batches = productBatchRepository.findByActiveTrueOrderByExpirationDate();
        int remaining = exitDTO.getQuantity();

        for (ProductBatch batch : batches) {
            if (!batch.getProduct().getId().equals(exitDTO.getProductId())) continue;
            if (remaining <= 0) break;

            int available = batch.getQuantity();
            if (available <= 0) continue;

            int toRemove = Math.min(available, remaining);
            batch.setQuantity(available - toRemove);

            if (batch.getQuantity() == 0) {
                batch.setActive(false);
                log.debug("Estoque - Lote ID {} esgotado e desativado", batch.getId());
            }

            productBatchRepository.save(batch);
            createMovement(batch, toRemove, exitDTO.getType(), user, exitDTO.getObservation());
            remaining -= toRemove;
        }

        log.info("Estoque - SAÍDA registrada. Produto: {}, Quantidade: {}, Tipo: {}, Usuário: {}",
                product.getName(), exitDTO.getQuantity(), exitDTO.getType(), user.getEmail());
    }

    @Override
    public Page<StockMovementResponseDTO> getMovements(Pageable pageable) {
        return stockMovementRepository.findAll(pageable)
                .map(stockMapper::toResponseDTO);
    }

    @Override
    public Page<StockMovementResponseDTO> getMovementsByProduct(Long productId, Pageable pageable) {
        return stockMovementRepository.findByBatchProductId(productId, pageable)
                .map(stockMapper::toResponseDTO);
    }

    @Override
    public Integer getCurrentStock(Long productId) {
        Integer stock = productBatchRepository.getTotalStockByProduct(productId);
        log.debug("Estoque - Consulta de estoque. Produto ID: {}, Estoque: {}", productId, stock);
        return stock;
    }

    private Product getProductOrThrow(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Estoque - Produto não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Produto não encontrado com ID: " + id);
                });
        if (!product.isActive()) {
            log.warn("Estoque - Tentativa de movimentar produto inativo. ID: {}, Nome: {}", id, product.getName());
            throw new BusinessException("Produto inativo: " + product.getName());
        }
        return product;
    }

    private User getUserOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Estoque - Usuário não encontrado. ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
                });
        if (!user.isActive()) {
            log.warn("Estoque - Tentativa de movimentação por usuário inativo. ID: {}, Email: {}", id, user.getEmail());
            throw new BusinessException("Usuário inativo: " + user.getEmail());
        }
        return user;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            log.warn("Estoque - Quantidade inválida: {}", quantity);
            throw new BusinessException("Quantidade deve ser maior que zero");
        }
    }

    private void validateExitType(StockMovementType type) {
        if (type != StockMovementType.SALE && type != StockMovementType.LOSS) {
            log.warn("Estoque - Tipo de saída inválido: {}", type);
            throw new BusinessException("Tipo inválido para saída. Use SALE ou LOSS");
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
        log.debug("Estoque - Movimentação criada. Lote ID: {}, Quantidade: {}, Tipo: {}", batch.getId(), quantity, type);
    }
}