package com.cartflow.product.repository;

import com.cartflow.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);
    Optional<Product> findByIdAndIsActiveTrue(Long id);
    Page<Product> findByIsActiveTrue(Pageable pageable);
}