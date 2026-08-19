package com.idempotentpayment.transaction.dto;

import com.idempotentpayment.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Outgoing DTO returned after processing a payment.
 * This is also the exact JSON object cached in the idempotency_store.
 */
public record PaymentResponse(
    Long transactionId,
    String transactionReference,
    Long sourceAccountId,
    Long targetAccountId,
    BigDecimal amount,
    String currency,
    TransactionStatus status,
    LocalDateTime createdAt
) {}
