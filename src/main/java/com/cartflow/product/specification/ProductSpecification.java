package com.cartflow.product.specification;

import com.cartflow.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(
            Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minRating) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Unconditionally exclude soft-deleted products
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minRating != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> withKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.isTrue(root.get("isActive"));
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            Predicate nameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), likePattern);
            Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), likePattern);

            return criteriaBuilder.and(
                    criteriaBuilder.isTrue(root.get("isActive")),
                    criteriaBuilder.or(nameMatch, descriptionMatch)
            );
        };
    }
}