package com.idempotentpayment.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox Scheduler.
 * 
 * Background worker that periodically polls 'pending_outbox' table for PENDING records,
 * sends them to the Mock PSP Client, and marks them SENT upon success.
 */
@Component
public class OutboxScheduler {

    // TODO: Inject OutboxRepository, MockPspClient

    // TODO: Implement scheduled polling method:
    // @Scheduled(fixedDelay = 5000) // Polls every 5 seconds
    // public void processOutboxEvents()
}
