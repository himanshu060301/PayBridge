package com.example.payment.api.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateId;   // paymentId
    private String eventType;     // PAYMENT_CREATED

    @Lob
    private String payload;       // JSON
    private boolean processed;
    private LocalDateTime createdAt;
}