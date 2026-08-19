package com.idempotentpayment.idempotency;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Idempotency Service.
 * 
 * Responsibilities:
 * 1. Checks if a key already exists in 'idempotency_store'.
 * 2. Hashes request payloads (SHA-256) to ensure the client didn't send a different payload with the same key.
 * 3. Saves the final response (JSON string) and status code for future duplicate requests.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    // TODO: Implement idempotency management methods:
    // - findExistingRecord(String key)
    // - lockOrStart(String key, Object requestPayload)
    // - saveSuccessfulResponse(String key, Object responsePayload, int statusCode)
}
