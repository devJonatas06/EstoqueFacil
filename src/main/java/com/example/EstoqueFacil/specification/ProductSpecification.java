package com.example.EstoqueFacil.specification;

import com.example.EstoqueFacil.dto.product.ProductFilterDTO;
import com.example.EstoqueFacil.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(ProductFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            if (filter.getBarcode() != null && !filter.getBarcode().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("barcode"), filter.getBarcode()));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salePrice"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salePrice"), filter.getMaxPrice()));
            }

            if (filter.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), filter.getActive()));
            } else {
                predicates.add(criteriaBuilder.isTrue(root.get("active")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}