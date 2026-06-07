package com.example.payment.api.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.payment.api.dto.PaymentWebhookRequest;
import com.example.payment.api.entity.*;
import com.example.payment.api.repository.*;
import com.example.payment.api.service.*;
import com.example.payment.api.util.WebhookPayloadUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final SignatureService signatureService;
    private final OrderService orderService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Override
    @Transactional
    public void handleWebhook(PaymentWebhookRequest request) {
        log.info("Webhook received. webhookId={}, paymentId={}",request.getWebhookId(),request.getPaymentId());
        String payload = WebhookPayloadUtil.buildPayload(request.getPaymentId(),request.getTransactionId(),request.getStatus());
        signatureService.validateSignature(payload,request.getSignature());
        
        if (webhookEventRepository.existsByWebhookId(request.getWebhookId())) {
            log.warn("Duplicate webhook received {}", request.getWebhookId());
            return;
        }

        WebhookEvent webhookEvent = WebhookEvent.builder()
        .webhookId(request.getWebhookId())
        .paymentId(request.getPaymentId())
        .eventType(request.getStatus())
        .payload(payload)
        .signature(request.getSignature())
        .status(WebhookStatus.RECEIVED)
        .retryCount(0)
        .createdAt(LocalDateTime.now())
        .build();

        webhookEventRepository.save(webhookEvent);
        try {

            Payment payment = paymentRepository.findByPaymentId(request.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setTransactionId(request.getTransactionId());
            payment.setGatewayReference(request.getGatewayReference());
            payment.setStatus(PaymentStatus.valueOf(request.getStatus().toUpperCase()));
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            paymentRepository.save(payment);

            IdempotencyKey key = idempotencyKeyRepository
            .findByPaymentId(payment.getPaymentId())
            .orElseThrow(() -> new RuntimeException("Idempotency key not found"));

            key.setPaymentStatus(payment.getStatus());
            key.setUpdatedAt(LocalDateTime.now());
            idempotencyKeyRepository.save(key);

            orderService.updateOrderStatus(payment.getOrderId(),payment.getStatus());

            webhookEvent.setStatus(WebhookStatus.PROCESSED);
            webhookEvent.setProcessedAt(LocalDateTime.now());
        } catch (Exception ex) {
            webhookEvent.setRetryCount(webhookEvent.getRetryCount() + 1);
            webhookEvent.setFailureReason(ex.getMessage());
            if (webhookEvent.getRetryCount() >= 3) {
                webhookEvent.setStatus(WebhookStatus.FAILED);
            } else {
                webhookEvent.setStatus(WebhookStatus.RETRY_PENDING);
                webhookEvent.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            }

            log.error("Webhook processing failed. webhookId={}",request.getWebhookId(),ex);
            throw ex;
        }

        webhookEventRepository.save(webhookEvent);
        log.info("Webhook processed successfully. paymentId={}",request.getPaymentId());
    }

}
