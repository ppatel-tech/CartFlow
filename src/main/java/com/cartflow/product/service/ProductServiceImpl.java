package com.cartflow.product.service;

import com.cartflow.brand.entity.Brand;
import com.cartflow.brand.repository.BrandRepository;
import com.cartflow.category.entity.Category;
import com.cartflow.category.repository.CategoryRepository;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.inventory.entity.Inventory;
import com.cartflow.inventory.repository.InventoryRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;
    private final InventoryRepository inventoryRepository;

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

        Inventory inventory = Inventory.builder()
                .product(savedProduct)
                .availableQuantity(0)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .build();

        inventoryRepository.save(inventory);

        log.info("Product created: {} (SKU: {})", savedProduct.getName(), savedProduct.getSku());
        log.info("Inventory initialized for product: {}", savedProduct.getSku());
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
        Page<Product> productsPage = productRepository.findByIsActiveTrue(pageable);
        return mapProductsPageToResponse(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProducts(
            Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minRating, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withFilters(
                categoryId, brandId, minPrice, maxPrice, minRating);

        Page<Product> productsPage = productRepository.findAll(spec, pageable);
        return mapProductsPageToResponse(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {

        Specification<Product> spec = ProductSpecification.withKeyword(keyword);

        Page<Product> productsPage = productRepository.findAll(spec, pageable);
        return mapProductsPageToResponse(productsPage);
    }


    private Page<ProductResponse> mapProductsPageToResponse(Page<Product> productsPage) {

        List<Product> products = productsPage.getContent();
        if (products.isEmpty()) {
            return productsPage.map(this::mapToResponse);
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();

        Map<Long, List<String>> imagesByProductId = productImageRepository
                .findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.groupingBy(
                        img -> img.getProduct().getId(),
                        Collectors.mapping(ProductImage::getImageUrl, Collectors.toList())
                ));

        Map<Long, Inventory> inventoryByProductId = inventoryRepository
                .findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(inv -> inv.getProduct().getId(), inv -> inv));

        List<Long> categoryIds = products.stream()
                .map(p -> p.getCategory().getId())
                .distinct()
                .toList();
        Map<Long, Category> categoryById = categoryRepository.findByIdIn(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<Long> brandIds = products.stream()
                .map(p -> p.getBrand().getId())
                .distinct()
                .toList();
        Map<Long, Brand> brandById = brandRepository.findByIdIn(brandIds)
                .stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));

        return productsPage.map(product -> {
            List<String> images = imagesByProductId.getOrDefault(product.getId(), List.of());
            boolean inStock = Optional.ofNullable(inventoryByProductId.get(product.getId()))
                    .map(inv -> inv.getAvailableQuantity() > 0)
                    .orElse(false);
            Category category = categoryById.get(product.getCategory().getId());
            Brand brand = brandById.get(product.getBrand().getId());

            return buildResponse(product, images, inStock, category, brand);
        });
    }

    private ProductResponse buildResponse(Product product, List<String> imageUrls, boolean inStock,
                                          Category category, Brand brand) {
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
                .categoryId(category.getId())
                .categoryName(category.getName())
                .brandId(brand.getId())
                .brandName(brand.getName())
                .imageUrls(imageUrls)
                .inStock(inStock)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> imageUrls = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        boolean inStock = inventoryRepository.findByProductId(product.getId())
                .map(inventory -> inventory.getAvailableQuantity() > 0)
                .orElse(false);

        return buildResponse(product, imageUrls, inStock, product.getCategory(), product.getBrand());
    }
}

