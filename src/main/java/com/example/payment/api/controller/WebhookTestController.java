package com.example.payment.api.controller;

import com.example.payment.api.dto.PaymentWebhookRequest;
import com.example.payment.api.service.SignatureService;
import com.example.payment.api.util.WebhookPayloadUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WebhookTestController {

    private final SignatureService signatureService;

    @PostMapping("/test/signature")
    public String generateSignature(@RequestBody PaymentWebhookRequest request) {
        String payload = WebhookPayloadUtil.buildPayload(request.getPaymentId(),request.getTransactionId(),request.getStatus());
        return signatureService.generateSignature(payload);
    }
}