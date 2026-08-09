package com.cartflow.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class WishlistItemResponse {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private boolean inStock;
}