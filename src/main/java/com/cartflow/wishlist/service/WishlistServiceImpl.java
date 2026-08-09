package com.cartflow.wishlist.service;

import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.inventory.repository.InventoryRepository;
import com.cartflow.product.entity.Product;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import com.cartflow.wishlist.dto.request.AddWishlistItemRequest;
import com.cartflow.wishlist.dto.response.WishlistItemResponse;
import com.cartflow.wishlist.dto.response.WishlistResponse;
import com.cartflow.wishlist.entity.Wishlist;
import com.cartflow.wishlist.entity.WishlistItem;
import com.cartflow.wishlist.repository.WishlistItemRepository;
import com.cartflow.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public WishlistResponse addItem(String email, AddWishlistItemRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        Product product = productRepository.findByIdAndIsActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wishlistItemRepository.existsByWishlistAndProduct(wishlist, product)) {
            throw new BusinessException("Product is already in your wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        wishlistItemRepository.save(item);

        log.info("Product {} added to wishlist for user: {}", product.getSku(), email);

        Wishlist refreshedWishlist = wishlistRepository.findByUser(user).orElseThrow();
        return buildWishlistResponse(refreshedWishlist);
    }

    @Override
    @Transactional
    public WishlistResponse removeItem(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        WishlistItem item = wishlistItemRepository.findByWishlistAndProduct(wishlist, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));

        wishlist.getItems().remove(item);
        wishlistRepository.save(wishlist);

        log.info("Product {} removed from wishlist for user: {}", productId, email);

        return buildWishlistResponse(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getMyWishlist(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        return buildWishlistResponse(wishlist);
    }

    private WishlistResponse buildWishlistResponse(Wishlist wishlist) {
        List<WishlistItemResponse> itemResponses = wishlist.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();

                    String imageUrl = product.getImages().isEmpty()
                            ? null
                            : product.getImages().get(0).getImageUrl();

                    boolean inStock = inventoryRepository.findByProduct(product)
                            .map(inv -> inv.getAvailableQuantity() > 0)
                            .orElse(false);

                    return WishlistItemResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .productImageUrl(imageUrl)
                            .price(product.getDiscountPrice() != null
                                    ? product.getDiscountPrice() : product.getPrice())
                            .inStock(inStock)
                            .build();
                })
                .toList();

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .items(itemResponses)
                .build();
    }
}