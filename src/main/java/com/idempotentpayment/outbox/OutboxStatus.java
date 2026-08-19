package com.idempotentpayment.outbox;

/**
 * Lifecycle status of an outbox event.
 */
public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
