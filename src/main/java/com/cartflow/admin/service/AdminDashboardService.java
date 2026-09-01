package com.cartflow.admin.service;

import com.cartflow.admin.dto.response.DashboardSummaryResponse;
import com.cartflow.inventory.repository.InventoryRepository;
import com.cartflow.order.entity.OrderStatus;
import com.cartflow.order.repository.OrderRepository;
import com.cartflow.product.repository.ProductRepository;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardSummaryResponse getDashboardSummary() {

        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatusIn(
                List.of(OrderStatus.CREATED, OrderStatus.CONFIRMED));
        long lowStockProducts = inventoryRepository.countLowStockProducts();

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(orderRepository.getTotalRevenue())
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }
}