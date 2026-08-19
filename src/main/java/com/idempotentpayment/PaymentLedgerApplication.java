package com.idempotentpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Idempotent Payment Ledger & Balance Reconciliation Engine.
 * 
 * Placing this class at the root package (com.idempotentpayment) ensures that Spring Boot's
 * component scan automatically detects all controllers, services, repositories, and configs
 * in all sub-packages (account, transaction, idempotency, outbox, reconciliation, etc.).
 */
@SpringBootApplication
// @EnableRetry // TODO: Uncomment once spring-retry dependency is fetched
@EnableScheduling
public class PaymentLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentLedgerApplication.class, args);
    }
}
