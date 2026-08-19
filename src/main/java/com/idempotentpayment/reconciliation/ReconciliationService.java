package com.idempotentpayment.reconciliation;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;

/**
 * Reconciliation Service.
 * 
 * Core reconciliation engine logic.
 * 
 * Responsibilities:
 * 1. Queries all successful transactions in our internal ledger for a given target date.
 * 2. Parses the external PSP settlement CSV file (or stream).
 * 3. Compares internal ledger records against external PSP records.
 * 4. Persists discrepancies into the 'reconciliation_errors' table.
 */
@Service
public class ReconciliationService {

    private final ReconciliationRepository reconciliationRepository;

    public ReconciliationService(ReconciliationRepository reconciliationRepository) {
        this.reconciliationRepository = reconciliationRepository;
    }

    // TODO: Implement reconciliation execution method:
    // public void reconcileDate(LocalDate date, InputStream pspCsvInputStream)
}
