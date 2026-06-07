package com.example.payment.api.util;

import com.example.payment.api.entity.PaymentMethod;
import java.util.UUID;

public class PaymentUtil {

    private static final String PAYMENT_PREFIX = "PAY_";

    private PaymentUtil() {}

    public static String generatePaymentId() {
        return PAYMENT_PREFIX + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public static PaymentMethod parsePaymentMethod(String paymentMethod) {
        try {
            return PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid payment method: "+ paymentMethod);
        }
    }
}