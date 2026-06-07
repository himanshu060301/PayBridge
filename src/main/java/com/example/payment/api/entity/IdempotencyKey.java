package com.example.payment.api.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String orderId;
    private String requestHash;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}