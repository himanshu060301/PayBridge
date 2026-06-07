package com.example.payment.api.kafka;

import com.example.payment.api.dto.*;
import com.example.payment.api.entity.*;
import com.example.payment.api.repository.PaymentRepository;
import com.example.payment.api.service.*;
import com.example.payment.api.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentRepository paymentRepository;
    private final MockGatewayService mockGatewayService;
    private final WebhookService webhookService;
    private final SignatureService signatureService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${spring.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) throws Exception {
        
        PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
        log.info("Payment event received. paymentId={}", event.getPaymentId());

        Payment payment = paymentRepository
                .findByPaymentId(event.getPaymentId())
                .orElseThrow();

        try {
            log.info("Calling gateway. paymentId={}", payment.getPaymentId());
            
            MockGatewayResponse response = mockGatewayService.processPayment(payment);
            processGatewayResponse(payment, response);
            
            log.info("Gateway processed successfully. paymentId={}, txnId={}",payment.getPaymentId(),response.getTransactionId());
            
            try {
                sendWebhook(response, payment);
            } catch (Exception webhookEx) {
                log.error("Webhook failed. paymentId={}",payment.getPaymentId(),webhookEx);
            }
        } catch (Exception ex) {
            log.error("Gateway processing failed. paymentId={}",payment.getPaymentId(),ex);
            handleGatewayFailure(payment, ex);
        }
    }

    private void sendWebhook(MockGatewayResponse response, Payment payment) {
        // Webhook Request
        PaymentWebhookRequest webhookRequest = PaymentWebhookRequest.builder()
            .webhookId(WebhookPayloadUtil.generateWebhookId())
            .paymentId(payment.getPaymentId())
            .transactionId(response.getTransactionId())
            .gatewayReference(response.getGatewayReference())
            .status(response.getStatus())
            .build();

        String payload = WebhookPayloadUtil.buildPayload(webhookRequest.getPaymentId(),webhookRequest.getTransactionId(),webhookRequest.getStatus());
        String signature = signatureService.generateSignature(payload);
        webhookRequest.setSignature(signature);
        webhookService.handleWebhook(webhookRequest);
    }

    private void processGatewayResponse(Payment payment,MockGatewayResponse response){
        payment.setTransactionId(response.getTransactionId());
        payment.setGatewayReference(response.getGatewayReference());
        payment.setGatewayStatus(response.getStatus());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private void handleGatewayFailure(Payment payment,Exception ex){
        int retryCount = payment.getRetryCount() == null ? 0 : payment.getRetryCount();

        retryCount++;
        payment.setRetryCount(retryCount);
        payment.setFailureReason(ex.getMessage());

        if (retryCount >= 3) {
            payment.setStatus(PaymentStatus.FAILED);
            log.error("Max retry exceeded. paymentId={}", payment.getPaymentId());
        } else {
            payment.setStatus(PaymentStatus.RETRY_PENDING);
            log.warn("Retry {} scheduled. paymentId={}", retryCount, payment.getPaymentId());
        }

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}