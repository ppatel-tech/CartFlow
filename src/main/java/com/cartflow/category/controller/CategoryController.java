package com.cartflow.category.controller;

import com.cartflow.category.dto.request.CategoryRequest;
import com.cartflow.category.dto.response.CategoryResponse;
import com.cartflow.category.service.CategoryService;
import com.cartflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response = categoryService.updateCategory(id, request);

        return ApiResponse.success("Category updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("Category deleted successfully", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ApiResponse.success("Category retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<Page<CategoryResponse>> getAllCategories(
            @ParameterObject Pageable pageable) {

        Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ApiResponse.success("Categories retrieved successfully", response);
    }

    @GetMapping("/admin")
    public ApiResponse<Page<CategoryResponse>> getAllCategoriesForAdmin(
            @ParameterObject Pageable pageable) {

        Page<CategoryResponse> response = categoryService.getAllCategoriesForAdmin(pageable);
        return ApiResponse.success("All categories retrieved successfully", response);
    }
}