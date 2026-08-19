package com.idempotentpayment.transaction;

import com.idempotentpayment.transaction.dto.PaymentRequest;
import com.idempotentpayment.transaction.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction Controller.
 * 
 * REST API entry point for executing payments.
 * 
 * Endpoint:
 * - POST /api/v1/transactions
 *   Header: Idempotency-Key: <UUID> (Required)
 *   Body: PaymentRequest JSON
 * 
 * Flow:
 * 1. Read 'Idempotency-Key' from header.
 * 2. Check IdempotencyService.
 *    - If key exists & completed -> return cached PaymentResponse immediately.
 *    - If new key -> execute TransactionService, cache result in IdempotencyService, return response.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    // TODO: Inject TransactionService and IdempotencyService

    // TODO: Implement:
    // @PostMapping
    // public ResponseEntity<PaymentResponse> processPayment(
    //     @RequestHeader("Idempotency-Key") String idempotencyKey,
    //     @RequestBody PaymentRequest request
    // )
}
