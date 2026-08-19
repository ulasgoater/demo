package com.idempotentpayment.idempotency;

/**
 * Status of an idempotency key lifecycle.
 * 
 * PROCESSING: Request is currently being executed by a thread.
 * COMPLETED: Request finished successfully and response is cached.
 */
public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED
}
