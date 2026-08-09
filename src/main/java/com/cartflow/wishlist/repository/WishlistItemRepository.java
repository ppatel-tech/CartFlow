package com.cartflow.wishlist.repository;

import com.cartflow.product.entity.Product;
import com.cartflow.wishlist.entity.Wishlist;
import com.cartflow.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    boolean existsByWishlistAndProduct(Wishlist wishlist, Product product);
    Optional<WishlistItem> findByWishlistAndProduct(Wishlist wishlist, Product product);
}