package com.idempotentpayment.reconciliation;

import com.idempotentpayment.transaction.TransactionRecord;
import com.idempotentpayment.transaction.TransactionRepository;
import com.idempotentpayment.transaction.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reconciliation Service.
 * 
 * Core reconciliation engine logic.
 * 
 * Responsibilities:
 * 1. Queries all transactions in our internal ledger for a given target date.
 * 2. Parses the external PSP settlement CSV file/stream.
 * 3. Compares internal ledger records against external PSP records (row-by-row and amounts).
 * 4. Persists discrepancies into the 'reconciliation_errors' table for financial auditing.
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final TransactionRepository transactionRepository;
    private final ReconciliationRepository reconciliationRepository;

    public ReconciliationService(
            TransactionRepository transactionRepository,
            ReconciliationRepository reconciliationRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    /**
     * Reconciles all transactions for a given date against a PSP settlement CSV input stream.
     * 
     * Expected CSV format:
     * transaction_reference,amount,currency,status
     */
    @Transactional
    public List<ReconciliationError> reconcileDate(LocalDate targetDate, InputStream pspCsvInputStream) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // 1. Fetch all internal ledger records for this date
        List<TransactionRecord> internalRecords = transactionRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        Map<String, TransactionRecord> internalMap = internalRecords.stream()
                .collect(Collectors.toMap(TransactionRecord::getTransactionReference, t -> t, (t1, t2) -> t1));

        Set<String> matchedInternalRefs = new HashSet<>();
        List<ReconciliationError> errors = new ArrayList<>();

        // 2. Parse external PSP CSV stream
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(pspCsvInputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Skip CSV header if present
                if (isHeader && (line.toLowerCase().startsWith("transaction") || line.toLowerCase().startsWith("ref"))) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                String[] columns = line.split(",");
                if (columns.length < 4) {
                    log.warn("[RECONCILIATION] Skipping invalid CSV line: {}", line);
                    continue;
                }

                String pspRef = columns[0].trim();
                BigDecimal pspAmount = new BigDecimal(columns[1].trim());
                String pspCurrency = columns[2].trim();
                String pspStatus = columns[3].trim();

                // 3. Compare with internal ledger
                if (!internalMap.containsKey(pspRef)) {
                    // MISSING_IN_LEDGER: PSP has a record that does not exist in our ledger!
                    ReconciliationError error = new ReconciliationError(
                            targetDate,
                            pspRef,
                            ReconciliationErrorType.MISSING_IN_LEDGER,
                            null,
                            pspAmount,
                            "Transaction present in PSP report but missing in internal ledger"
                    );
                    errors.add(error);
                } else {
                    TransactionRecord internalRecord = internalMap.get(pspRef);
                    matchedInternalRefs.add(pspRef);

                    // Check amount mismatch
                    if (internalRecord.getAmount().compareTo(pspAmount) != 0) {
                        ReconciliationError error = new ReconciliationError(
                                targetDate,
                                pspRef,
                                ReconciliationErrorType.AMOUNT_MISMATCH,
                                internalRecord.getAmount(),
                                pspAmount,
                                String.format("Amount mismatch: Internal [%s] vs PSP [%s]", internalRecord.getAmount(), pspAmount)
                        );
                        errors.add(error);
                    }

                    // Check status mismatch
                    if (!internalRecord.getStatus().name().equalsIgnoreCase(pspStatus)) {
                        ReconciliationError error = new ReconciliationError(
                                targetDate,
                                pspRef,
                                ReconciliationErrorType.STATUS_MISMATCH,
                                internalRecord.getAmount(),
                                pspAmount,
                                String.format("Status mismatch: Internal [%s] vs PSP [%s]", internalRecord.getStatus(), pspStatus)
                        );
                        errors.add(error);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RECONCILIATION] Failed to parse PSP reconciliation CSV", e);
            throw new RuntimeException("Error during CSV parsing in reconciliation", e);
        }

        // 4. Find transactions in our DB that the PSP CSV missed (MISSING_IN_PSP)
        for (TransactionRecord internal : internalRecords) {
            if (internal.getStatus() == TransactionStatus.SUCCESS && !matchedInternalRefs.contains(internal.getTransactionReference())) {
                ReconciliationError error = new ReconciliationError(
                        targetDate,
                        internal.getTransactionReference(),
                        ReconciliationErrorType.MISSING_IN_PSP,
                        internal.getAmount(),
                        null,
                        "Transaction marked SUCCESS in internal ledger but missing in external PSP report"
                );
                errors.add(error);
            }
        }

        // 5. Persist all discovered errors for audit
        if (!errors.isEmpty()) {
            reconciliationRepository.saveAll(errors);
            log.warn("[RECONCILIATION] Completed with {} discrepancies found for date {}", errors.size(), targetDate);
        } else {
            log.info("[RECONCILIATION] 100% Match! 0 discrepancies found for date {}", targetDate);
        }

        return errors;
    }
}
