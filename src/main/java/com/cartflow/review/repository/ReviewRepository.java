package com.cartflow.review.repository;

import com.cartflow.product.entity.Product;
import com.cartflow.review.entity.Review;
import com.cartflow.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductAndUser(Product product, User user);
    Optional<Review> findByIdAndUser(Long id, User user);
    Page<Review> findByProduct(Product product, Pageable pageable);
    List<Review> findByProduct(Product product);
}