package com.cartflow.product.service;

import com.cartflow.brand.entity.Brand;
import com.cartflow.brand.repository.BrandRepository;
import com.cartflow.category.entity.Category;
import com.cartflow.category.repository.CategoryRepository;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.product.dto.request.ProductRequest;
import com.cartflow.product.dto.response.ProductResponse;
import com.cartflow.product.entity.Product;
import com.cartflow.product.entity.ProductImage;
import com.cartflow.product.repository.ProductImageRepository;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                    "A product with SKU '" + request.getSku() + "' already exists");
        }

        validateDiscountPrice(request.getPrice(), request.getDiscountPrice());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .averageRating(BigDecimal.ZERO)
                .totalReviews(0)
                .isActive(true)
                .category(category)
                .brand(brand)
                .build();

        Product savedProduct = productRepository.save(product);

        log.info("Product created: {} (SKU: {})", savedProduct.getName(), savedProduct.getSku());

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                    "A product with SKU '" + request.getSku() + "' already exists");
        }

        validateDiscountPrice(request.getPrice(), request.getDiscountPrice());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setCategory(category);
        product.setBrand(brand);

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated: {} (SKU: {})", updatedProduct.getName(), updatedProduct.getSku());

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setActive(false);
        productRepository.save(product);

        log.info("Product soft-deleted: {} (SKU: {})", product.getName(), product.getSku());
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void addProductImage(Long productId, MultipartFile file) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (file.isEmpty()) {
            throw new BusinessException("Uploaded file is empty");
        }

        String imageUrl = fileStorageService.storeFile(file);

        int nextOrder = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId).size();

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .displayOrder(nextOrder)
                .build();

        productImageRepository.save(image);

        log.info("Image added to product {}: {}", productId, imageUrl);
    }

    private void validateDiscountPrice(BigDecimal price, BigDecimal discountPrice) {
        if (discountPrice != null && discountPrice.compareTo(price) >= 0) {
            throw new BusinessException("Discount price must be less than the regular price");
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProducts(
            Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minRating, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withFilters(
                categoryId, brandId, minPrice, maxPrice, minRating);

        return productRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withKeyword(keyword);

        return productRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> imageUrls = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .averageRating(product.getAverageRating())
                .totalReviews(product.getTotalReviews())
                .isActive(product.isActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brandId(product.getBrand().getId())
                .brandName(product.getBrand().getName())
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt())
                .build();
    }
}