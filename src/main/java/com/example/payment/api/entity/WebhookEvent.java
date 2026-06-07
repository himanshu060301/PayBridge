package com.example.payment.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id",unique = true,nullable = false)
    private String webhookId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "event_type")
    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    private String signature;
    private Integer retryCount;
    private String failureReason;
    private LocalDateTime nextRetryAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}