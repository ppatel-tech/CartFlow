package com.cartflow.product.controller;

import com.cartflow.common.ApiResponse;
import com.cartflow.product.dto.request.ProductRequest;
import com.cartflow.product.dto.response.ProductResponse;
import com.cartflow.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }


    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        ProductResponse response = productService.updateProduct(id, request);

        return ApiResponse.success("Product updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success("Product deleted successfully", null);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> addProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        productService.addProductImage(id, file);

        return ApiResponse.success("Image uploaded successfully", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ApiResponse.success("Product retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getAllProducts(@ParameterObject Pageable pageable) {
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        return ApiResponse.success("Products retrieved successfully", response);
    }

    @GetMapping("/filter")
    public ApiResponse<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @ParameterObject Pageable pageable) {

        Page<ProductResponse> response = productService.filterProducts(
                categoryId, brandId, minPrice, maxPrice, minRating, pageable);

        return ApiResponse.success("Filtered products retrieved successfully", response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @ParameterObject Pageable pageable) {

        Page<ProductResponse> response = productService.searchProducts(keyword, pageable);

        return ApiResponse.success("Search results retrieved successfully", response);
    }
}

