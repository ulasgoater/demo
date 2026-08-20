package com.idempotentpayment.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TransactionRecord Entity.
 * 
 * Immutable ledger entry representing every monetary transfer.
 * In a financial system, once a transaction is recorded, it should never be deleted or mutated.
 */
@Entity
@Table(name = "transactions")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "source_account_id", nullable = false)
    private Long sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private Long targetAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TransactionRecord() {}

    public TransactionRecord(String transactionReference, Long sourceAccountId, Long targetAccountId,
                             BigDecimal amount, String currency, TransactionStatus status) {
        this.transactionReference = transactionReference;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return this.id;
    }
    public String getTransactionReference() {
        return this.transactionReference;
    }
    public Long getSourceAccountId() {
        return this.sourceAccountId;   
    }
    public Long getTargetAccountId() {
        return this.targetAccountId;
    }
    public BigDecimal getAmount() {
        return this.amount;
    }
    public String getCurrency() {
        return this.currency;
    }
    public TransactionStatus getStatus() {
        return this.status;
    }
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
    
    // No setters provided to maintain immutability of the transaction record after creation.
}
