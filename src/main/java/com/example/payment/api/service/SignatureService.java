package com.example.payment.api.service;

public interface SignatureService {
    String generateSignature(String payload);
    void validateSignature(String payload,String signature);
}
