package com.example.payment.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String description;
}