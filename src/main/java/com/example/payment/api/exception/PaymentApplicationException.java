package com.example.payment.api.exception;

public class PaymentApplicationException extends RuntimeException {

    private final ErrorType errorType;

    public PaymentApplicationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public PaymentApplicationException(
            ErrorType errorType,
            String message,
            Throwable cause) {

        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}