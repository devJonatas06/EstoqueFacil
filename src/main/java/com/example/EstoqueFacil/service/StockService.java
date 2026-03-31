package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.dto.stock.StockMovementResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {

    void registerEntry(StockEntryDTO entryDTO);

    void registerExit(StockExitDTO exitDTO);

    Page<StockMovementResponseDTO> getMovements(Pageable pageable);

    Page<StockMovementResponseDTO> getMovementsByProduct(Long productId, Pageable pageable);

    Integer getCurrentStock(Long productId);
}