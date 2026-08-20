package com.idempotentpayment.transaction;

import com.idempotentpayment.common.exception.DuplicateIdempotencyKeyException;
import com.idempotentpayment.idempotency.IdempotencyRecord;
import com.idempotentpayment.idempotency.IdempotencyService;
import com.idempotentpayment.idempotency.IdempotencyStatus;
import com.idempotentpayment.transaction.dto.PaymentRequest;
import com.idempotentpayment.transaction.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;

    public TransactionController(TransactionService transactionService, IdempotencyService idempotencyService) {
        this.transactionService = transactionService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PaymentRequest request
    ) {
        // Step 1: Validate Idempotency-Key header is present
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Header 'Idempotency-Key' is required");
        }

        // Step 2: Compute SHA-256 hash of the incoming request payload
        String currentHash = idempotencyService.hashRequestPayload(request);

        // Step 3: Check if key already exists in idempotency_store
        Optional<IdempotencyRecord> existingOpt = idempotencyService.findByIdempotencyKey(idempotencyKey);

        if (existingOpt.isPresent()) {
            IdempotencyRecord existing = existingOpt.get();

            // A. Payload Tampering / Mismatch Check:
            // If the same key is submitted with different transfer parameters, reject it!
            if (!existing.getRequestHash().equals(currentHash)) {
                throw new DuplicateIdempotencyKeyException(
                        "Idempotency-Key '" + idempotencyKey + "' was previously used with a different request payload"
                );
            }

            // B. Concurrent In-Flight Check:
            // If another thread is actively running this exact transaction, tell client to retry shortly
            if (existing.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new DuplicateIdempotencyKeyException(
                        "Payment with Idempotency-Key '" + idempotencyKey + "' is currently being processed. Please retry shortly."
                );
            }

            // C. Return Cached Response:
            PaymentResponse cachedResponse = idempotencyService.parseResponse(
                    existing.getResponseBody(),
                    PaymentResponse.class
            );
            return ResponseEntity.ok(cachedResponse);
        }

        // Step 4: First time seeing this key -> Lock key as PROCESSING
        idempotencyService.startProcessing(idempotencyKey, currentHash);

        // Step 5: Execute atomic money transfer in database
        PaymentResponse response = transactionService.executeTransfer(request);

        // Step 6: Save response JSON into idempotency_store and mark COMPLETED
        idempotencyService.saveSuccessfulResponse(idempotencyKey, response, 200);

        return ResponseEntity.ok(response);
    }
}
