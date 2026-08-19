package com.idempotentpayment.common.exception;

/**
 * Thrown when an operation targets an account ID that does not exist in the database.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
