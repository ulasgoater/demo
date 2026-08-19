package com.idempotentpayment.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Service.
 * 
 * Writes outbox event records into the database within the calling database transaction.
 */
@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    // TODO: Implement helper to record an event:
    // @Transactional(propagation = Propagation.MANDATORY) // Ensures caller has an active transaction!
    // public void publishEvent(String aggregateType, String aggregateId, String eventType, Object payload)
}
