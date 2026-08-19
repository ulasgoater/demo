package com.idempotentpayment.common.exception;

/**
 * Thrown when an account does not have sufficient balance to perform a debit/transfer.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
