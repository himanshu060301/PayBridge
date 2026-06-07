package com.example.payment.api.entity;

public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}