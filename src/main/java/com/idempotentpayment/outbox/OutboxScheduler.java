package com.idempotentpayment.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outbox Scheduler.
 * 
 * Background worker that periodically polls 'pending_outbox' table for PENDING records,
 * sends them to the Mock PSP Client, and marks them SENT upon success.
 */
@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxRepository outboxRepository;
    private final MockPspClient mockPspClient;

    public OutboxScheduler(OutboxRepository outboxRepository, MockPspClient mockPspClient) {
        this.outboxRepository = outboxRepository;
        this.mockPspClient = mockPspClient;
    }

    /**
     * Background scheduled polling worker.
     * Runs every 5 seconds, finds PENDING outbox records, calls the external Mock PSP,
     * and updates the status to SENT upon success.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        // 1. Fetch top 20 oldest PENDING records from OutboxRepository
        List<OutboxRecord> pendingRecords = outboxRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingRecords.isEmpty()) {
            return;
        }

        log.info("[OUTBOX SCHEDULER] Found {} pending events to deliver to PSP", pendingRecords.size());

        // 2. Process each record independently
        for (OutboxRecord record : pendingRecords) {
            try {
                // Call external Mock PSP over simulated network
                boolean delivered = mockPspClient.sendNotification(record.getAggregateId(), record.getPayload());

                if (delivered) {
                    record.markSent();
                    log.info("[OUTBOX SCHEDULER] Event [{}] successfully delivered and marked SENT", record.getAggregateId());
                } else {
                    record.markFailed();
                    log.warn("[OUTBOX SCHEDULER] Event [{}] delivery failed, incrementing retry count", record.getAggregateId());
                }
            } catch (Exception e) {
                // Catching exception prevents 1 failed event from crashing the entire batch of 20!
                record.markFailed();
                log.error("[OUTBOX SCHEDULER] Error delivering event [{}] to PSP: {}", record.getAggregateId(), e.getMessage(), e);
            }
            outboxRepository.save(record);
        }
    }
}
