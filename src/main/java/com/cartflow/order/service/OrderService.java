package com.cartflow.order.service;

import com.cartflow.order.dto.request.CheckoutRequest;
import com.cartflow.order.dto.response.OrderConfigResponse;
import com.cartflow.order.dto.response.OrderResponse;
import com.cartflow.order.dto.response.OrderTrackingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse checkout(String email, CheckoutRequest request);
    OrderResponse getOrderById(String email, Long orderId);
    Page<OrderResponse> getMyOrders(String email, Pageable pageable);
    OrderResponse cancelOrder(String email, Long orderId);
    OrderTrackingResponse trackOrder(String email, Long orderId);
    byte[] getInvoicePdf(String email, Long orderId);
    OrderConfigResponse getOrderConfig();
}