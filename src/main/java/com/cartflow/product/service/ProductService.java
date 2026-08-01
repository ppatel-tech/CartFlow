package com.cartflow.product.service;

import com.cartflow.product.dto.request.ProductRequest;
import com.cartflow.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    void addProductImage(Long productId, org.springframework.web.multipart.MultipartFile file);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> filterProducts(
            Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minRating, Pageable pageable);
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);
}