package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.stock.StockEntryDTO;
import com.example.EstoqueFacil.dto.stock.StockExitDTO;
import com.example.EstoqueFacil.entity.Product;

import java.util.List;

public interface StockService {

    void registerEntry(StockEntryDTO entryDTO);

    void registerExit(StockExitDTO exitDTO);

    List<Product> getLowStockProducts();
}