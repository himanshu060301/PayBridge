package com.example.payment.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockGatewayResponse {

    private String transactionId;
    private String gatewayReference;
    private String status;
    private String message;
}