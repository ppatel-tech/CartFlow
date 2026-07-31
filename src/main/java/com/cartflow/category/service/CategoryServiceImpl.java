package com.cartflow.category.service;

import com.cartflow.category.dto.request.CategoryRequest;
import com.cartflow.category.dto.response.CategoryResponse;
import com.cartflow.category.entity.Category;
import com.cartflow.category.repository.CategoryRepository;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "A category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        log.info("Category created: {}", savedCategory.getName());

        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "A category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated: {}", updatedCategory.getName());

        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setActive(false);
        categoryRepository.save(category);

        log.info("Category soft-deleted: {}", category.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CategoryResponse> getAllCategoriesForAdmin(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .build();
    }
}