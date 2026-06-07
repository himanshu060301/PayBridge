package com.example.payment.api.service.impl;

import com.example.payment.api.dto.MockGatewayResponse;
import com.example.payment.api.entity.Payment;
import com.example.payment.api.service.MockGatewayService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class MockGatewayServiceImpl implements MockGatewayService {

    @Override
    public MockGatewayResponse processPayment(Payment payment) {
        String status = generateRandomStatus();

        return MockGatewayResponse.builder()
                .transactionId(generateTransactionId())
                .gatewayReference(generateGatewayReference())
                .status(status)
                .message("Payment " + status)
                .build();
    }

    private String generateRandomStatus() {
        int random = new Random().nextInt(3);

        return switch (random) {
            case 0 -> "SUCCESS";
            case 1 -> "FAILED";
            default -> "PENDING";
        };
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private String generateGatewayReference() {
        return "GTW_" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}