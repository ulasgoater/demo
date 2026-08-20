package com.idempotentpayment.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
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
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Finds an existing record by its idempotency key.
     */
    public Optional<IdempotencyRecord> findByIdempotencyKey(String key) {
        return idempotencyRepository.findByIdempotencyKey(key);
    }

    /**
     * Locks the key in the database with status PROCESSING in its own independent transaction.
     * Uses REQUIRES_NEW propagation so the lock is committed immediately, visible to any concurrent threads.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startProcessing(String key, String requestHash) {
        IdempotencyRecord record = new IdempotencyRecord(key, requestHash, IdempotencyStatus.PROCESSING);
        idempotencyRepository.saveAndFlush(record);
    }

    /**
     * Marks the record as COMPLETED and stores the serialized JSON response body.
     */
    @Transactional
    public void saveSuccessfulResponse(String key, Object responsePayload, int statusCode) {
        try {
            String responseJson = objectMapper.writeValueAsString(responsePayload);
            IdempotencyRecord record = idempotencyRepository.findByIdempotencyKey(key)
                    .orElseThrow(() -> new RuntimeException("Idempotency record not found for key: " + key));
            record.markCompleted(responseJson, statusCode);
            idempotencyRepository.save(record);
        } catch (Exception e) {
            throw new RuntimeException("Error saving successful response", e);
        }
    }

    /**
     * Deserializes cached JSON string back into a typed response DTO (e.g. PaymentResponse).
     */
    public <T> T parseResponse(String json, Class<T> targetClass) {
        try {
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cached response", e);
        }
    }

    // Why is the input a java object but not string? 
    // By the time our code runs, Spring has already converted the raw JSON into a Java PaymentRequest object.
    // By declaring hashRequestPayload(Object requestPayload):
    // Object is the parent class of everything in Java.
    // This makes our method generic and reusable: today it can hash a PaymentRequest, tomorrow it can hash a RefundRequest, a CreateAccountRequest, or any other object without writing duplicate code.
    public String hashRequestPayload(Object requestPayload) {
        try {
            String json = objectMapper.writeValueAsString(requestPayload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing request payload", e);
        }
    }
}
