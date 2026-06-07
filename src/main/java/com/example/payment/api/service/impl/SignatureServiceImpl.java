package com.example.payment.api.service.impl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.payment.api.service.SignatureService;
import java.util.Base64;

@Service
public class SignatureServiceImpl implements SignatureService {

    @Value("${payment.gateway.secret}")
    private String secret;
    private static final String HMAC_SHA256 = "HmacSHA256";

    public String generateSignature(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);

            SecretKeySpec secretKeySpec = new SecretKeySpec(
                            secret.getBytes(),
                            HMAC_SHA256
                    );

            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception ex) {
            throw new RuntimeException("Error generating signature");
        }
    }


    @Override
    public void validateSignature(String payload,String signature) {
        String generatedSignature = generateSignature(payload);

        if (!generatedSignature.equals(signature)) {
            throw new RuntimeException("Invalid webhook signature");
        }
    }
}