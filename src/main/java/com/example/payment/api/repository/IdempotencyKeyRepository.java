package com.example.payment.api.repository;

import com.example.payment.api.entity.IdempotencyKey;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByIdempotencyKey(String key);
    boolean existsByIdempotencyKey(String key);
    Optional<IdempotencyKey> findByPaymentId(String paymentId);
}