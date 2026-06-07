package com.example.payment.api.service;

import com.example.payment.api.dto.*;
import com.example.payment.api.entity.PaymentStatus;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);
    void updateOrderStatus(String orderId,PaymentStatus paymentStatus);
}