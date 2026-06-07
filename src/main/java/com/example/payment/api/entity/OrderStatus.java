package com.example.payment.api.entity;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED
}