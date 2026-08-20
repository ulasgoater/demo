package com.idempotentpayment.idempotency;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IdempotencyRecord Entity.
 * 
 * Maps to 'idempotency_store' table.
 * 
 * Purpose:
 * Stores the unique idempotency key, a hash of the request payload (to detect payload tampering),
 * and the serialized JSON response body.
 */
@Entity
@Table(name = "idempotency_store")
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Lob // 255 characters is not enough for JSON responses, so we use a Large Object (LOB) to store the response body.
    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String requestHash, IdempotencyStatus status) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // TODO: Getters, Setters, and helper to complete record:
    // public void markCompleted(String responseBody, Integer statusCode)
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    public String getRequestHash() {
        return requestHash;
    }
    public String getResponseBody() {
        return responseBody;
        }
    public Integer getStatusCode() {
        return statusCode;
    }
    public IdempotencyStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void markCompleted(String responseBody, Integer statusCode) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.status = IdempotencyStatus.COMPLETED;
    }
}
