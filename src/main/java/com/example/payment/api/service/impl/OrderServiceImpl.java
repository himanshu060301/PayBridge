package com.example.payment.api.service.impl;

import com.example.payment.api.dto.*;
import com.example.payment.api.entity.*;
import com.example.payment.api.repository.OrderRepository;
import com.example.payment.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
            .orderId("ORD_" + UUID.randomUUID().toString().substring(0, 8))
            .userId(request.getUserId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .description(request.getDescription())
            .status(OrderStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    public void updateOrderStatus(String orderId,PaymentStatus paymentStatus) {
        Order order = orderRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(mapOrderStatus(paymentStatus));
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }
    
    private CreateOrderResponse mapToResponse(Order order) {
        return CreateOrderResponse.builder()
            .orderId(order.getOrderId())
            .userId(order.getUserId())
            .amount(order.getAmount())
            .currency(order.getCurrency())
            .status(order.getStatus())
            .description(order.getDescription())
            .createdAt(order.getCreatedAt())
            .build();
    }

    private OrderStatus mapOrderStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case SUCCESS -> OrderStatus.PAID;
            case FAILED -> OrderStatus.PAYMENT_FAILED;
            case PENDING -> OrderStatus.PAYMENT_PENDING;
            default -> OrderStatus.CREATED;
        };
    }
}