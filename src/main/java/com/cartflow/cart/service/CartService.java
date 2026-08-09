package com.cartflow.cart.service;

import com.cartflow.cart.dto.request.AddCartItemRequest;
import com.cartflow.cart.dto.request.UpdateCartItemRequest;
import com.cartflow.cart.dto.response.CartResponse;

public interface CartService {
    CartResponse addItemToCart(String email, AddCartItemRequest request);
    CartResponse getMyCart(String email);
    CartResponse updateItemQuantity(String email, Long itemId, UpdateCartItemRequest request);
    CartResponse removeItem(String email, Long itemId);
    void clearCart(String email);
}