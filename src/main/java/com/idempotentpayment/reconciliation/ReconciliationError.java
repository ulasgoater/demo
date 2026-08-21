package com.idempotentpayment.reconciliation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReconciliationError Entity.
 * 
 * Maps to 'reconciliation_errors' table.
 * 
 * Purpose:
 * Stores discrepancies detected during the daily reconciliation audit
 * for compliance, accounting, and operational investigation.
 */
@Entity
@Table(name = "reconciliation_errors")
public class ReconciliationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type", nullable = false)
    private ReconciliationErrorType errorType;

    @Column(name = "internal_amount")
    private BigDecimal internalAmount;

    @Column(name = "psp_amount")
    private BigDecimal pspAmount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ReconciliationError() {}

    public ReconciliationError(LocalDate reconciliationDate, String transactionReference,
                               ReconciliationErrorType errorType, BigDecimal internalAmount,
                               BigDecimal pspAmount, String description) {
        this.reconciliationDate = reconciliationDate;
        this.transactionReference = transactionReference;
        this.errorType = errorType;
        this.internalAmount = internalAmount;
        this.pspAmount = pspAmount;
        this.description = description;
        this.resolved = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markResolved() {
        this.resolved = true;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public LocalDate getReconciliationDate() {
        return reconciliationDate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public ReconciliationErrorType getErrorType() {
        return errorType;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public BigDecimal getPspAmount() {
        return pspAmount;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
