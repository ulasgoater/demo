package com.idempotentpayment.reconciliation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Reconciliation Scheduler.
 * 
 * Scheduled cron job that runs daily at midnight (00:00:00) to reconcile
 * yesterday's transactions against the PSP's daily settlement report.
 */
@Component
public class ReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationScheduler.class);

    private final ReconciliationService reconciliationService;
    private final MockPspCsvGenerator mockPspCsvGenerator;

    public ReconciliationScheduler(
            ReconciliationService reconciliationService,
            MockPspCsvGenerator mockPspCsvGenerator
    ) {
        this.reconciliationService = reconciliationService;
        this.mockPspCsvGenerator = mockPspCsvGenerator;
    }

    /**
     * Runs daily at midnight (or can be triggered programmatically).
     * Reconciles yesterday's transactions against mock PSP CSV settlement data.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyReconciliation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[RECONCILIATION SCHEDULER] Starting automated daily audit for {}", yesterday);

        try {
            // Generate or fetch the settlement CSV from the PSP
            // In testing/mock mode, we pass a sample CSV or empty report
            String sampleCsv = "transaction_reference,amount,currency,status\n";
            InputStream csvStream = mockPspCsvGenerator.generateSampleReportCsv(sampleCsv);

            List<ReconciliationError> errors = reconciliationService.reconcileDate(yesterday, csvStream);
            log.info("[RECONCILIATION SCHEDULER] Audit finished for {}. Discrepancies logged: {}", yesterday, errors.size());
        } catch (Exception e) {
            log.error("[RECONCILIATION SCHEDULER] Error during scheduled reconciliation for date {}", yesterday, e);
        }
    }
}
