package com.cartflow.inventory.repository;

import com.cartflow.inventory.entity.Inventory;
import com.cartflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= i.lowStockThreshold")
    long countLowStockProducts();
}