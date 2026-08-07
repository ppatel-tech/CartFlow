package com.cartflow.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private boolean isActive;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Instant createdAt;
    private List<String> imageUrls;
    private boolean inStock;
}