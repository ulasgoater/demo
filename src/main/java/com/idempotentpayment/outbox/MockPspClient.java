package com.idempotentpayment.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock PSP (Payment Service Provider) Client.
 * 
 * Simulates making an external HTTP webhook/REST call to an external payment processor
 * (like Stripe, Adyen, or a partner bank) to notify them of completed transactions.
 */
@Component
public class MockPspClient {

    private static final Logger log = LoggerFactory.getLogger(MockPspClient.class);

    /**
     * Simulates sending a webhook notification to an external PSP.
     * In real life, this would use RestClient or WebClient to make an HTTP POST.
     */
    public boolean sendNotification(String transactionReference, String payload) {
        log.info("[MOCK PSP] Successfully delivered notification for transaction ref: {}", transactionReference);
        return true;
    }
}
