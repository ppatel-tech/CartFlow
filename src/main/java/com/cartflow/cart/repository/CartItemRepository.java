package com.cartflow.cart.repository;

import com.cartflow.cart.entity.Cart;
import com.cartflow.cart.entity.CartItem;
import com.cartflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    Optional<CartItem> findByIdAndCart(Long id, Cart cart);
}