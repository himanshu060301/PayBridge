package com.example.payment.api.repository;

import com.example.payment.api.entity.Payment;
import com.example.payment.api.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    long countByOrderId(String orderId);
    boolean existsByOrderIdAndStatus(String orderId,PaymentStatus status);
    Optional<Payment> findByPaymentId(String paymentId);
    int countByOrderIdAndStatusIn(String orderId, List<PaymentStatus> statuses);
    List<Payment> findByStatus(PaymentStatus retryPending);
}