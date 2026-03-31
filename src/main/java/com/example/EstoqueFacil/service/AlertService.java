package com.example.EstoqueFacil.service;

import com.example.EstoqueFacil.dto.report.AlertDetailDTO;
import com.example.EstoqueFacil.dto.report.AlertSummaryDTO;
import com.example.EstoqueFacil.dto.report.LowStockProductDTO;
import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;

import java.util.List;

public interface AlertService {

    List<Product> getLowStockProducts();

    List<Product> getInactiveProducts(int days);

    List<ProductBatch> getExpiringBatches(int days);

    List<ProductBatch> getExpiredBatches();

    List<Product> getCriticalStockProducts(int days);

    AlertSummaryDTO getAlertSummary();

    AlertDetailDTO getAlertDetails();

    List<LowStockProductDTO> getLowStockProductsDTO();
}