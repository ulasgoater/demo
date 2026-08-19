package com.idempotentpayment.transaction;

/**
 * Status of a transaction in the ledger.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
