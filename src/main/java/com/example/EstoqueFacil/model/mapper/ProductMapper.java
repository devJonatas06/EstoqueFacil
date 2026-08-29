package com.example.EstoqueFacil.model.mapper;

import com.example.EstoqueFacil.model.dto.request.product.ProductCreateRequestDTO;
import com.example.EstoqueFacil.model.dto.response.product.ProductResponseDTO;
import com.example.EstoqueFacil.model.dto.request.product.ProductUpdateRequestDTO;
import com.example.EstoqueFacil.model.entity.Product;
import com.example.EstoqueFacil.repository.ProductBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductBatchRepository productBatchRepository;

    public Product toEntity(ProductCreateRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setBarcode(dto.getBarcode());
        product.setDescription(dto.getDescription());
        product.setMaker(dto.getMaker());
        product.setCostPrice(dto.getCostPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setMinimumStock(dto.getMinimumStock());
        return product;
    }

    public ProductResponseDTO toResponseDTO(Product product) {
        Integer currentStock = productBatchRepository.getTotalStockByProduct(product.getId());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .description(product.getDescription())
                .maker(product.getMaker())
                .costPrice(product.getCostPrice())
                .salePrice(product.getSalePrice())
                .profitMargin(product.getSalePrice().subtract(product.getCostPrice()))
                .minimumStock(product.getMinimumStock())
                .currentStock(currentStock)
                .stockStatus(calculateStockStatus(currentStock, product.getMinimumStock()))
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public void updateEntity(Product product, ProductUpdateRequestDTO dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getMaker() != null) product.setMaker(dto.getMaker());
        if (dto.getCostPrice() != null) product.setCostPrice(dto.getCostPrice());
        if (dto.getSalePrice() != null) product.setSalePrice(dto.getSalePrice());
        if (dto.getMinimumStock() != null) product.setMinimumStock(dto.getMinimumStock());
    }

    private String calculateStockStatus(Integer currentStock, Integer minimumStock) {
        if (currentStock == null || minimumStock == null) return "INDISPONÍVEL";
        if (currentStock >= minimumStock) return "OK";
        if (currentStock >= minimumStock * 0.5) return "BAIXO";
        return "CRÍTICO";
    }
}