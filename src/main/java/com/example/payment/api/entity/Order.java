package com.example.payment.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true)
    private String orderId;

    private Long userId;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}