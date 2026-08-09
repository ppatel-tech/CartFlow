package com.cartflow.wishlist.service;

import com.cartflow.wishlist.dto.request.AddWishlistItemRequest;
import com.cartflow.wishlist.dto.response.WishlistResponse;

public interface WishlistService {
    WishlistResponse addItem(String email, AddWishlistItemRequest request);
    WishlistResponse removeItem(String email, Long productId);
    WishlistResponse getMyWishlist(String email);
}