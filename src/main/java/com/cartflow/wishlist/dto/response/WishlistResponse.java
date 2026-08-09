package com.cartflow.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WishlistResponse {
    private Long id;
    private List<WishlistItemResponse> items;
}