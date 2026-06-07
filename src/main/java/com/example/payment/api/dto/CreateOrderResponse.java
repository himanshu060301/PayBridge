package com.example.payment.api.dto;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.payment.api.entity.OrderStatus;

import jakarta.persistence.Enumerated;

@Data
@Builder
public class CreateOrderResponse {

    private String orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String description;
    private LocalDateTime createdAt;
}