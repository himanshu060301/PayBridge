package com.example.payment.api.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}