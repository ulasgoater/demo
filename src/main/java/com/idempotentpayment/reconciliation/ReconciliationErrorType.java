package com.idempotentpayment.reconciliation;

/**
 * Types of discrepancies discovered by the Reconciliation Engine.
 */
public enum ReconciliationErrorType {
    MISSING_IN_PSP,      // Present in internal ledger, but missing in external PSP report
    MISSING_IN_LEDGER,   // Present in external PSP report, but missing in internal ledger
    AMOUNT_MISMATCH,     // Transaction exists in both, but amounts differ
    STATUS_MISMATCH      // Transaction exists in both, but statuses differ
}
