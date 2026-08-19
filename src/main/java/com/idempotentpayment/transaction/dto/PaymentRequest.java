package com.idempotentpayment.transaction.dto;

import java.math.BigDecimal;

/**
 * Incoming DTO for initiating a money transfer between accounts.
 * 
 * Fields:
 * - sourceAccountId: ID of the account to deduct funds from
 * - targetAccountId: ID of the account to credit funds to
 * - amount: Amount to transfer (must be positive)
 * - currency: ISO currency code (e.g. "USD")
 */
public record PaymentRequest(
    Long sourceAccountId,
    Long targetAccountId,
    BigDecimal amount,
    String currency
) {}
