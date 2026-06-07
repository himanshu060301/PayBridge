package com.example.payment.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentWebhookRequest {
    private String webhookId;
    private String paymentId;
    private String transactionId;
    private String gatewayReference;
    private String status;
    private String signature;
}