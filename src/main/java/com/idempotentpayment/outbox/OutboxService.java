package com.idempotentpayment.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Propagation;

/**
 * Outbox Service.
 * 
 * Writes outbox event records into the database within the calling database transaction.
 */
@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    
    public OutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    // TODO: Implement helper to record an event:
    // @Transactional(propagation = Propagation.MANDATORY) // Ensures caller has an active transaction!
    // public void publishEvent(String aggregateType, String aggregateId, String eventType, Object payload)
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxRecord record = new OutboxRecord(aggregateType, aggregateId, eventType, json);
            outboxRepository.save(record);
        } catch (Exception e) {
            throw new RuntimeException("Failed to record outbox event", e);
        }
    }
}
