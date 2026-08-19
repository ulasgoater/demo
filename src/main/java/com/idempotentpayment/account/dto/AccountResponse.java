package com.idempotentpayment.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned to clients representing the public state of an account.
 * 
 * Notice: We don't expose internal DB details unless needed.
 */
public record AccountResponse(
    Long id,
    String accountNumber,
    BigDecimal balance,
    String currency,
    LocalDateTime createdAt
) {}
