package com.cartflow.brand.repository;

import com.cartflow.brand.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByName(String name);
    Optional<Brand> findByIdAndIsActiveTrue(Long id);
    Page<Brand> findByIsActiveTrue(Pageable pageable);
}