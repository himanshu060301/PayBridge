package com.example.payment.api.service;

import com.example.payment.api.dto.MockGatewayResponse;
import com.example.payment.api.entity.Payment;

public interface MockGatewayService {
    MockGatewayResponse processPayment(Payment payment);
}