package com.cartflow.category.service;

import com.cartflow.category.dto.request.CategoryRequest;
import com.cartflow.category.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    CategoryResponse getCategoryById(Long id);
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    Page<CategoryResponse> getAllCategoriesForAdmin(Pageable pageable);
}