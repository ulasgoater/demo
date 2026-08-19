package com.idempotentpayment.reconciliation;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Mock PSP CSV Generator.
 * 
 * Helper utility that generates a simulated CSV settlement report string/stream
 * matching typical PSP formats (e.g. transaction_ref, amount, currency, status, timestamp).
 * Can be configured with intentional discrepancies for testing.
 */
@Component
public class MockPspCsvGenerator {

    /**
     * Generates a sample CSV input stream for testing reconciliation.
     * Format: transaction_reference,amount,currency,status,settled_at
     */
    public InputStream generateSampleReportCsv(String csvContent) {
        return new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
    }
}
