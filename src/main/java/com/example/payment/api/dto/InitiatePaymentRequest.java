package com.example.payment.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InitiatePaymentRequest {

    private String orderId;
    private String orderDesc;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}