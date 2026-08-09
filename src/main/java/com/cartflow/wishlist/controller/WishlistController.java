package com.cartflow.wishlist.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.security.UserPrincipal;
import com.cartflow.wishlist.dto.request.AddWishlistItemRequest;
import com.cartflow.wishlist.dto.response.WishlistResponse;
import com.cartflow.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse<WishlistResponse> getMyWishlist(
            @AuthenticationPrincipal UserPrincipal principal) {

        WishlistResponse response = wishlistService.getMyWishlist(principal.getEmail());
        return ApiResponse.success("Wishlist retrieved successfully", response);
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddWishlistItemRequest request) {

        WishlistResponse response = wishlistService.addItem(principal.getEmail(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to wishlist", response));
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<WishlistResponse> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {

        WishlistResponse response = wishlistService.removeItem(principal.getEmail(), productId);
        return ApiResponse.success("Item removed from wishlist", response);
    }
}