package com.cartflow.cart.service;

import com.cartflow.cart.dto.request.AddCartItemRequest;
import com.cartflow.cart.dto.request.UpdateCartItemRequest;
import com.cartflow.cart.dto.response.CartItemResponse;
import com.cartflow.cart.dto.response.CartResponse;
import com.cartflow.cart.entity.Cart;
import com.cartflow.cart.entity.CartItem;
import com.cartflow.cart.repository.CartItemRepository;
import com.cartflow.cart.repository.CartRepository;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.product.entity.Product;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse addItemToCart(String email, AddCartItemRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Product product = productRepository.findByIdAndIsActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        BigDecimal effectivePrice = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getPrice();

        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                            existingItem.setUnitPrice(effectivePrice);
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .unitPrice(effectivePrice)
                                    .build();
                            cartItemRepository.save(newItem);
                        }
                );

        log.info("Product {} added/updated in cart for user: {}", product.getSku(), email);

        return buildCartResponse(cart);
    }



    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return buildCartResponse(cart);
    }


    @Override
    @Transactional
    public CartResponse updateItemQuantity(String email, Long itemId, UpdateCartItemRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        log.info("Cart item {} quantity updated to {} for user: {}", itemId, request.getQuantity(), email);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String email, Long itemId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartRepository.save(cart);

        log.info("Cart item {} removed for user: {}", itemId, email);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cart cleared for user: {}", email);
    }






    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cart.getItems();

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> {
                    BigDecimal subtotal = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    String imageUrl = item.getProduct().getImages().isEmpty()
                            ? null
                            : item.getProduct().getImages().get(0).getImageUrl();

                    return CartItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .productImageUrl(imageUrl)
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }
}