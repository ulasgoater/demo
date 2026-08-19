package com.idempotentpayment.reconciliation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reconciliation Scheduler.
 * 
 * Scheduled cron job that runs daily at midnight (e.g. 00:00:00) to reconcile
 * yesterday's transactions against the PSP's daily settlement report.
 */
@Component
public class ReconciliationScheduler {

    // TODO: Inject ReconciliationService and MockPspCsvGenerator

    // TODO: Implement cron job:
    // @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight (or configurable)
    // public void runDailyReconciliation()
}
