package com.cartflow.cart.controller;

import com.cartflow.cart.dto.request.AddCartItemRequest;
import com.cartflow.cart.dto.request.UpdateCartItemRequest;
import com.cartflow.cart.dto.response.CartResponse;
import com.cartflow.cart.service.CartService;
import com.cartflow.common.ApiResponse;
import com.cartflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request) {

        CartResponse response = cartService.addItemToCart(principal.getEmail(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", response));
    }


    @GetMapping
    public ApiResponse<CartResponse> getMyCart(
            @AuthenticationPrincipal UserPrincipal principal) {

        CartResponse response = cartService.getMyCart(principal.getEmail());

        return ApiResponse.success("Cart retrieved successfully", response);
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartResponse response = cartService.updateItemQuantity(principal.getEmail(), itemId, request);
        return ApiResponse.success("Cart item updated successfully", response);
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId) {

        CartResponse response = cartService.removeItem(principal.getEmail(), itemId);
        return ApiResponse.success("Item removed from cart", response);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getEmail());
        return ApiResponse.success("Cart cleared successfully", null);
    }
}