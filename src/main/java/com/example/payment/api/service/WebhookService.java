package com.example.payment.api.service;

import com.example.payment.api.dto.PaymentWebhookRequest;

public interface WebhookService {
    void handleWebhook(PaymentWebhookRequest request);
}
