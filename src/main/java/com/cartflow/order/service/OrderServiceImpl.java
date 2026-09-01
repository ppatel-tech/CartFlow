package com.cartflow.order.service;

import com.cartflow.address.entity.Address;
import com.cartflow.address.repository.AddressRepository;
import com.cartflow.cart.entity.Cart;
import com.cartflow.cart.entity.CartItem;
import com.cartflow.cart.repository.CartRepository;
import com.cartflow.coupon.service.CouponService;
import com.cartflow.exception.BusinessException;
import com.cartflow.exception.OutOfStockException;
import com.cartflow.exception.ResourceNotFoundException;
import com.cartflow.inventory.entity.Inventory;
import com.cartflow.inventory.repository.InventoryRepository;
import com.cartflow.notification.service.NotificationService;
import com.cartflow.order.dto.request.CheckoutRequest;
import com.cartflow.order.dto.response.OrderConfigResponse;
import com.cartflow.order.dto.response.OrderItemResponse;
import com.cartflow.order.dto.response.OrderResponse;
import com.cartflow.order.dto.response.OrderTrackingResponse;
import com.cartflow.order.entity.Order;
import com.cartflow.order.entity.OrderItem;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.entity.PaymentStatus;
import com.cartflow.order.repository.OrderItemRepository;
import com.cartflow.order.repository.OrderRepository;
import com.cartflow.payment.repository.PaymentRepository;
import com.cartflow.product.entity.Product;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.user.entity.User;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponService couponService;
    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    
    @Value("${app.order.tax-rate}")
    private BigDecimal taxRate;

    @Value("${app.order.shipping-charge}")
    private BigDecimal shippingCharge;

    @Override
    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout with an empty cart");
        }

        Address address = addressRepository.findByIdAndUser(request.getAddressId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Pass 1: validate every item has sufficient stock before touching anything
        List<Inventory> inventoriesToReserve = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findByIdAndIsActiveTrue(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(
                            "Product '" + cartItem.getProduct().getName() + "' is no longer available"));

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() -> new OutOfStockException(
                            "Product '" + product.getName() + "' is out of stock"));

            if (inventory.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new OutOfStockException(
                        "Only " + inventory.getAvailableQuantity() + " unit(s) of '"
                                + product.getName() + "' available");
            }

            inventoriesToReserve.add(inventory);
        }

        // Calculate subtotal from cart's own snapshotted prices
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            discount = couponService.validateAndCalculateDiscount(request.getCouponCode(), subtotal, user);
        }

        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = subtotal.subtract(discount).add(tax).add(shippingCharge);

        // Pass 2: now that everything is validated, actually reserve inventory
        for (int i = 0; i < cart.getItems().size(); i++) {
            CartItem cartItem = cart.getItems().get(i);
            Inventory inventory = inventoriesToReserve.get(i);

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - cartItem.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() + cartItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(user)
                .shippingFullName(address.getFullName())
                .shippingPhone(address.getPhone())
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingCountry(address.getCountry())
                .shippingPostalCode(address.getPostalCode())
                .subtotal(subtotal)
                .discount(discount)
                .tax(tax)
                .shippingCharge(shippingCharge)
                .finalAmount(finalAmount)
                .couponCode(request.getCouponCode())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(savedOrder)
                        .product(cartItem.getProduct())
                        .productName(cartItem.getProduct().getName())
                        .quantity(cartItem.getQuantity())
                        .sellingPrice(cartItem.getUnitPrice())
                        .build())
                .toList();

        orderItemRepository.saveAll(orderItems);

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            couponService.recordCouponUsage(request.getCouponCode(), user);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order {} created for user: {} (total: {})",
                savedOrder.getOrderNumber(), email, finalAmount);

        notificationService.notifyUser(
                user,
                "Order placed: " + savedOrder.getOrderNumber(),
                "Hi " + user.getFirstName() + ", your order " + savedOrder.getOrderNumber()
                        + " has been placed successfully. Total: ₹" + finalAmount,
                true
        );

        return buildOrderResponse(savedOrder, orderItems);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomer(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrder(order);

        return buildOrderResponse(order, items);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String email, Pageable pageable) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByCustomer(user, pageable)
                .map(order -> buildOrderResponse(order, orderItemRepository.findByOrder(order)));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomer(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getOrderStatus() != OrderStatus.CREATED
                && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(
                    "Order cannot be cancelled once it has reached " + order.getOrderStatus() + " status");
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);

        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProduct(item.getProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product"));

            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            paymentRepository.findByOrder(order).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            });
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            log.info("Order {} was paid - marked as refunded on cancellation", order.getOrderNumber());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} cancelled for user: {}", order.getOrderNumber(), email);

        return buildOrderResponse(updatedOrder, items);
    }


    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse trackOrder(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomer(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return OrderTrackingResponse.builder()
                .orderNumber(order.getOrderNumber())
                .currentStatus(order.getOrderStatus())
                .lastUpdated(order.getUpdatedAt())
                .statusDescription(describeStatus(order.getOrderStatus()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getInvoicePdf(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndCustomer(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrder(order);

        return invoiceService.generateInvoicePdf(order, items);
    }

    @Override
    public OrderConfigResponse getOrderConfig() {
        return OrderConfigResponse.builder()
                .taxRate(taxRate)
                .shippingCharge(shippingCharge)
                .build();
    }



    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKING),
            OrderStatus.PACKING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED)
    );

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus current = order.getOrderStatus();
        Set<OrderStatus> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowedNext.contains(newStatus)) {
            throw new BusinessException(
                    "Cannot move order from " + current + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrder(updatedOrder);

        notificationService.notifyUser(
                order.getCustomer(),
                "Order " + order.getOrderNumber() + " update",
                "Your order status is now: " + newStatus,
                true
        );

        log.info("Order {} status changed to {}", order.getOrderNumber(), newStatus);

        return buildOrderResponse(updatedOrder, items);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(order -> buildOrderResponse(order, orderItemRepository.findByOrder(order)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return buildOrderResponse(order, orderItemRepository.findByOrder(order));
    }

    private String describeStatus(OrderStatus status) {
        return switch (status) {
            case CREATED -> "Your order has been placed and is awaiting confirmation";
            case CONFIRMED -> "Your order has been confirmed and will be packed soon";
            case PACKING -> "Your order is being packed";
            case SHIPPED -> "Your order has been shipped";
            case OUT_FOR_DELIVERY -> "Your order is out for delivery";
            case DELIVERED -> "Your order has been delivered";
            case CANCELLED -> "This order has been cancelled";
        };
    }



    private OrderResponse buildOrderResponse(Order order, List<OrderItem> items) {

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .sellingPrice(item.getSellingPrice())
                        .subtotal(item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(itemResponses)
                .shippingFullName(order.getShippingFullName())
                .shippingPhone(order.getShippingPhone())
                .shippingStreet(order.getShippingStreet())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingCountry(order.getShippingCountry())
                .shippingPostalCode(order.getShippingPostalCode())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .shippingCharge(order.getShippingCharge())
                .finalAmount(order.getFinalAmount())
                .couponCode(order.getCouponCode())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}