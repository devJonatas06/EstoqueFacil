package com.example.EstoqueFacil.dto.report;

import com.example.EstoqueFacil.entity.Product;
import com.example.EstoqueFacil.entity.ProductBatch;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AlertSummary {
    private int lowStockCount;
    private int inactiveProductsCount;
    private int expiringBatchesCount;
    private int expiredBatchesCount;
    private int criticalStockCount;

    private List<Product> lowStockProducts;
    private List<Product> inactiveProducts;
    private List<ProductBatch> expiringBatches;
    private List<ProductBatch> expiredBatches;
    private List<Product> criticalStockProducts;
}