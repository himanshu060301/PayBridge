package com.example.payment.api.controller;

import com.example.payment.api.dto.*;
import com.example.payment.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookService webhookService;

    @PostMapping("/payments/initiate")
    public PaymentResponse initiatePayment(@RequestBody InitiatePaymentRequest request,
        @RequestHeader("Idempotency-Key") String key) {
            return paymentService.initiatePayment(request, key);
    }

    @PostMapping("/webhook")
    public String paymentWebhook(@RequestBody PaymentWebhookRequest request) {
        webhookService.handleWebhook(request);
        return "Webhook processed successfully";
    }
}