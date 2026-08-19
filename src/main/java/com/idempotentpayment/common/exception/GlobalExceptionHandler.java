package com.idempotentpayment.common.exception;

import com.idempotentpayment.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global Exception Handler (Controller Advice).
 * 
 * Intercepts unhandled exceptions thrown across all @RestController classes and translates
 * them into structured HTTP responses (e.g. 400 Bad Request, 404 Not Found, 409 Conflict).
 * 
 * You will configure custom exception handlers here.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO: Add @ExceptionHandler methods for:
    // 1. AccountNotFoundException -> 404 NOT FOUND
    // 2. InsufficientFundsException -> 400 BAD REQUEST
    // 3. DuplicateIdempotencyKeyException -> 409 CONFLICT
    // 4. ObjectOptimisticLockingFailureException -> 409 or 503 if retries are exhausted
    // 5. Generic Exception -> 500 INTERNAL SERVER ERROR
}
