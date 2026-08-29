package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.model.dto.request.stock.StockEntryRequestDTO;
import com.example.EstoqueFacil.model.dto.request.stock.StockExitRequestDTO;
import com.example.EstoqueFacil.model.dto.response.stock.StockMovementResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {

    void registerEntry(StockEntryRequestDTO entryDTO);

    void registerExit(StockExitRequestDTO exitDTO);

    Page<StockMovementResponseDTO> getMovements(Pageable pageable);

    Page<StockMovementResponseDTO> getMovementsByProduct(Long productId, Pageable pageable);

    Integer getCurrentStock(Long productId);
}