package com.example.payment.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;

public class IdempotencyUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generateHash(Object request) {
        try {
            String json = mapper.writeValueAsString(request);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(json.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash");
        }
    }
}