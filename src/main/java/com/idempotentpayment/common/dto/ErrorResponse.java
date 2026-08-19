package com.idempotentpayment.common.dto;

import java.time.LocalDateTime;

/**
 * Standardized API Error Response DTO.
 * 
 * Used across the entire application to return consistent JSON error objects
 * to API consumers instead of raw stack traces or default Spring error pages.
 * 
 * Example JSON output:
 * {
 *   "timestamp": "2026-08-19T15:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Insufficient funds in account",
 *   "path": "/api/v1/transactions"
 * }
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
