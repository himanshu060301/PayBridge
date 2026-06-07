package com.example.payment.api.kafka;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.example.payment.api.dto.PaymentEvent;
import com.example.payment.api.entity.*;
import com.example.payment.api.repository.OutboxRepository;
import com.example.payment.api.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRetryScheduler {
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @Scheduled(fixedDelay = 60000)
    public void retryPayments() {
        
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.RETRY_PENDING);
       
        payments.forEach(payment -> {

            PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .build();

            String payload;

            try {
                payload = objectMapper.writeValueAsString(event);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize event", e);
            }

            OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(payment.getPaymentId())
                .eventType("PAYMENT_CREATED")
                .payload(payload)
                .processed(false)
                .createdAt(LocalDateTime.now())
                .build();

            outboxRepository.save(outboxEvent);

            log.info("Retry event created. paymentId={}",payment.getPaymentId());
        });
    }
}
