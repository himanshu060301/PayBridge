package com.example.payment.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;

public class WebhookPayloadUtil {

    private static final String WEBHOOK_PREFIX = "WEBHOOK_";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private WebhookPayloadUtil() {}

    public static String buildPayload(String paymentId, String transactionId, String status) {
        try {
            Map<String, String> payload = Map.of(
                    "paymentId", paymentId,
                    "transactionId", transactionId,
                    "status", status
            );

            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new RuntimeException("Error building payload");
        }
    }

    public static String generateWebhookId() {
        return WEBHOOK_PREFIX + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}