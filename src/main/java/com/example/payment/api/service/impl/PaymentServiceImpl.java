package com.example.payment.api.service.impl;

import com.example.payment.api.dto.*;
import com.example.payment.api.entity.*;
import com.example.payment.api.exception.ErrorType;
import com.example.payment.api.exception.PaymentApplicationException;
import com.example.payment.api.repository.*;
import com.example.payment.api.service.*;
import com.example.payment.api.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${payment.gateway.name}")
    private String GATEWAY_NAME;
    private static final long EXPIRY_HOURS = 24;

    private final PaymentRepository paymentRepository;
    private final IdempotencyKeyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    // =========================================================
    // INITIATE PAYMENT
    // =========================================================
    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, String idempotencyKey) {

        String requestHash = IdempotencyUtil.generateHash(request);

        // 1. Check cached response first
        PaymentResponse cached = getCachedResponse(idempotencyKey, requestHash);
        if (cached != null) {
            return cached;
        }

        // 2. Validate business rule
        validateDuplicatePayment(request.getOrderId());

        // 3. Create payment first
        Payment payment = createInitialPayment(request);
        payment = paymentRepository.save(payment);
        log.info("SAVED PAYMENT ID = {}", payment.getPaymentId());

        // 4. Save idempotency record (atomic protection)
        saveInProgress(idempotencyKey, requestHash, request.getOrderId(), payment);

        // 5. Create Outbox event
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
            throw new PaymentApplicationException(ErrorType.EVENT_SERIALIZATION,"Failed to serialize event",e);
        }
        
        OutboxEvent outbox = OutboxEvent.builder()
                .aggregateId(payment.getPaymentId())
                .eventType("PAYMENT_CREATED")
                .payload(payload)
                .processed(false)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            outboxRepository.save(outbox);
            PaymentResponse response = mapToResponse(payment);
            markCompleted(idempotencyKey, response);

            return response;
        } catch (Exception ex) {
            markFailed(idempotencyKey,payment.getStatus());
            throw ex;
        }
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================
    private Payment createInitialPayment(InitiatePaymentRequest request) {

        int retryCount = calculateRetryCount(request.getOrderId());

        Payment payment = Payment.builder()
                .paymentId(PaymentUtil.generatePaymentId())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(PaymentUtil.parsePaymentMethod(request.getPaymentMethod()))
                .status(PaymentStatus.INITIATED)
                .gatewayName(GATEWAY_NAME)
                .retryCount(retryCount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return payment;
    }

    // =========================================================
    // DUPLICATE PAYMENT CHECK
    // =========================================================
    private void validateDuplicatePayment(String orderId) {

        boolean exists = paymentRepository.existsByOrderIdAndStatus(
                orderId,
                PaymentStatus.SUCCESS
        );

        if (exists) {
            throw new PaymentApplicationException(ErrorType.PAYMENT,"Payment already completed for orderId: " + orderId);
        }
    }

    // =========================================================
    // RETRY COUNT
    // =========================================================
    private int calculateRetryCount(String orderId) {

        return paymentRepository.countByOrderIdAndStatusIn(
                orderId,
                List.of(
                        PaymentStatus.INITIATED,
                        PaymentStatus.PENDING,
                        PaymentStatus.FAILED
                )
        );
    }

    // =========================================================
    // CACHE RESPONSE (IDEMPOTENCY READ)
    // =========================================================
    private PaymentResponse getCachedResponse(String key, String requestHash) {

        IdempotencyKey record = idempotencyRepository.findByIdempotencyKey(key)
                .orElse(null);

        if (record == null) {
            return null;
        }

        if (record.getCreatedAt().isBefore(LocalDateTime.now().minusHours(EXPIRY_HOURS))) {
            throw new PaymentApplicationException(ErrorType.IDEMPOTENCY,"Idempotency key expired");
        }

        if (record.getRequestHash() != null && !record.getRequestHash().equals(requestHash)) {
            throw new PaymentApplicationException(ErrorType.IDEMPOTENCY,"Idempotency key reused with different request");
        }

        if (record.getStatus() == IdempotencyStatus.PROCESSING) {
            throw new PaymentApplicationException(ErrorType.IDEMPOTENCY,"Request already in progress");
        }

        if (record.getStatus() == IdempotencyStatus.COMPLETED) {
            return PaymentResponse.builder()
                    .paymentId(record.getPaymentId())
                    .orderId(record.getOrderId())
                    .status(record.getPaymentStatus())
                    .build();
        }

        return null;
    }

    // =========================================================
    // SAVE IDEMPOTENCY START
    // =========================================================
    private void saveInProgress(String key, String hash, String orderId, Payment payment) {

        IdempotencyKey record = IdempotencyKey.builder()
                .idempotencyKey(key)
                .requestHash(hash)
                .orderId(orderId)
                .paymentId(payment.getPaymentId())
                .status(IdempotencyStatus.PROCESSING)
                .paymentStatus(payment.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            idempotencyRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException ex) {
            IdempotencyKey existing = idempotencyRepository.findByIdempotencyKey(key).orElseThrow();

            if (!existing.getRequestHash().equals(hash)) {
                throw new PaymentApplicationException(ErrorType.IDEMPOTENCY,"Idempotency key reused with different request");
            }
            throw new PaymentApplicationException(ErrorType.IDEMPOTENCY,"Request already in progress");
        }
    }

    // =========================================================
    // MARK COMPLETED
    // =========================================================
    private void markCompleted(String key, PaymentResponse response) {

        IdempotencyKey record = idempotencyRepository
                .findByIdempotencyKey(key)
                .orElseThrow();

        record.setPaymentStatus(response.getStatus());
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setPaymentId(response.getPaymentId());
        record.setUpdatedAt(LocalDateTime.now());

        idempotencyRepository.save(record);
    }

    // =========================================================
    // MARK FAILED
    // =========================================================
    private void markFailed(String key, PaymentStatus status) {

        IdempotencyKey record = idempotencyRepository.findByIdempotencyKey(key)
                .orElseThrow();

        record.setStatus(IdempotencyStatus.FAILED);
        record.setPaymentStatus(status);
        record.setUpdatedAt(LocalDateTime.now());

        idempotencyRepository.save(record);
    }

    // =========================================================
    // RESPONSE MAPPER
    // =========================================================
    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .status(payment.getStatus())
                .gatewayName(payment.getGatewayName())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}