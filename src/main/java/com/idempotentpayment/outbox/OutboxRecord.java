package com.idempotentpayment.outbox;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OutboxRecord Entity.
 * 
 * Maps to 'pending_outbox' table.
 * 
 * Implements the Transactional Outbox Pattern:
 * Instead of making an external HTTP call directly during the payment transaction,
 * we write an event row to this table in the exact same DB transaction.
 */
@Entity
@Table(name = "pending_outbox")
public class OutboxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType; // e.g., "TRANSACTION"

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;   // e.g., transactionReference

    @Column(name = "event_type", nullable = false)
    private String eventType;     // e.g., "PAYMENT_COMPLETED"

    @Lob
    @Column(nullable = false)
    private String payload;        // JSON serialized event payload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public OutboxRecord() {}

    public OutboxRecord(String aggregateType, String aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // TODO: Getters, Setters, and helper methods:
    // public void markSent()
    // public void markFailed()
}
