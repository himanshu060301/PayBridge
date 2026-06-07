package com.example.payment.api.service;

import com.example.payment.api.dto.InitiatePaymentRequest;
import com.example.payment.api.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse initiatePayment(InitiatePaymentRequest request, String key);
}