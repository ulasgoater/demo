package com.idempotentpayment.common.exception;

/**
 * Thrown when an incoming request uses an Idempotency-Key with a different payload
 * than the one previously recorded for that key (Payload Conflict).
 */
public class DuplicateIdempotencyKeyException extends RuntimeException {
    public DuplicateIdempotencyKeyException(String message) {
        super(message);
    }
}
