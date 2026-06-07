package com.example.payment.api.entity;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    RETRY_PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED
}