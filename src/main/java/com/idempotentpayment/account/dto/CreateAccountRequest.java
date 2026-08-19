package com.idempotentpayment.account.dto;

import java.math.BigDecimal;

/**
 * DTO received when creating a new bank account.
 * 
 * Fields:
 * - accountNumber: unique identifier string for the account (e.g., "ACC-1001")
 * - initialBalance: opening balance (BigDecimal)
 * - currency: ISO currency code (e.g., "USD", "EUR")
 */
public record CreateAccountRequest(
    String accountNumber,
    BigDecimal initialBalance,
    String currency
) {}
