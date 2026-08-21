package com.idempotentpayment.outbox;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;



/**
 * Outbox Scheduler.
 * 
 * Background worker that periodically polls 'pending_outbox' table for PENDING records,
 * sends them to the Mock PSP Client, and marks them SENT upon success.
 */
@Component
public class OutboxScheduler {

    // TODO: Inject OutboxRepository, MockPspClient
    private final OutboxRepository outboxRepository;
    private final MockPspClient mockPspClient;
    private static final Logger log= LoggerFactory.getLogger(OutboxScheduler.class);
    public OutboxScheduler(OutboxRepository outboxRepository, MockPspClient mockPspClient) {
        this.outboxRepository = outboxRepository;
        this.mockPspClient = mockPspClient;
    }

    // TODO: Implement scheduled polling method:
    // @Scheduled(fixedDelay = 5000) // Polls every 5 seconds
    // public void processOutboxEvents()
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents(){
        // 1. Fetch PENDING records from OutboxRepository
        List<OutboxRecord> pendingRecords = outboxRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        // 2. For each record:
        for(OutboxRecord record :pendingRecords){
            try {
            //    a. Send to MockPspClient
            boolean delivered =mockPspClient.sendNotification(record.getAggregateId(),record.getPayload());
            if(delivered){
                record.markSent();

            }
            else record.markFailed();
               } 
               
            catch (Exception e) {
               log.error("Failed to process outbox event: {}", record.getAggregateId(), e);
            }
            outboxRepository.save(record);
        }
        
        //    b. If successful, mark record as SENT and update processedAt timestamp
        //    c. If failed, increment retryCount and optionally log the failure
    }
}
