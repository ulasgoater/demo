package com.idempotentpayment;

import com.idempotentpayment.account.Account;
import com.idempotentpayment.account.AccountRepository;
import com.idempotentpayment.account.AccountService;
import com.idempotentpayment.account.dto.AccountResponse;
import com.idempotentpayment.account.dto.CreateAccountRequest;
import com.idempotentpayment.common.exception.DuplicateIdempotencyKeyException;
import com.idempotentpayment.idempotency.IdempotencyRepository;
import com.idempotentpayment.idempotency.IdempotencyService;
import com.idempotentpayment.outbox.OutboxRecord;
import com.idempotentpayment.outbox.OutboxRepository;
import com.idempotentpayment.outbox.OutboxScheduler;
import com.idempotentpayment.outbox.OutboxStatus;
import com.idempotentpayment.reconciliation.*;
import com.idempotentpayment.transaction.TransactionController;
import com.idempotentpayment.transaction.TransactionRecord;
import com.idempotentpayment.transaction.TransactionRepository;
import com.idempotentpayment.transaction.TransactionService;
import com.idempotentpayment.transaction.TransactionStatus;
import com.idempotentpayment.transaction.dto.PaymentRequest;
import com.idempotentpayment.transaction.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-End Integration Test Suite for the Idempotent Payment Ledger.
 */
@SpringBootTest
public class PaymentLedgerIntegrationTest {

    // Autowired builds the application context and injects the required beans for testing.
    // This allows us to test the integration of multiple components in a real Spring Boot environment.
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionController transactionController;
    @Autowired private IdempotencyService idempotencyService;
    @Autowired private IdempotencyRepository idempotencyRepository;
    @Autowired private OutboxRepository outboxRepository;
    @Autowired private OutboxScheduler outboxScheduler;
    @Autowired private ReconciliationService reconciliationService;
    @Autowired private ReconciliationRepository reconciliationRepository;
    @Autowired private MockPspCsvGenerator mockPspCsvGenerator;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        transactionRepository.deleteAll();
        idempotencyRepository.deleteAll();
        reconciliationRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Test 1: Should successfully transfer funds, update balances, and create ledger and outbox records")
    void testSuccessfulPayment_DeductsBalancesAndCreatesLedgerAndOutbox() {
        // 1. ARRANGE: Create 2 accounts
        AccountResponse accA = accountService.createAccount(
                new CreateAccountRequest("ACC-A", new BigDecimal("100.00"), "USD")
        );
        AccountResponse accB = accountService.createAccount(
                new CreateAccountRequest("ACC-B", new BigDecimal("50.00"), "USD")
        );

        // 2. ACT: Transfer $30 from Account A to Account B
        PaymentRequest paymentRequest = new PaymentRequest(
                accA.id(),
                accB.id(),
                new BigDecimal("30.00"),
                "USD"
        );
        PaymentResponse response = transactionService.executeTransfer(paymentRequest);

        // 3. ASSERT:
        // A. Verify response DTO
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("30.00"));

        // B. Verify database balances
        Account updatedA = accountService.getAccountEntity(accA.id());
        Account updatedB = accountService.getAccountEntity(accB.id());
        assertThat(updatedA.getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(updatedB.getBalance()).isEqualByComparingTo(new BigDecimal("80.00"));

        // C. Verify transaction ledger entry
        List<TransactionRecord> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getTransactionReference()).isEqualTo(response.transactionReference());

        // D. Verify outbox event record
        List<OutboxRecord> outboxEvents = outboxRepository.findAll();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(outboxEvents.get(0).getAggregateId()).isEqualTo(response.transactionReference());
    }

    @Test
    @DisplayName("Test 2: Duplicate request with same Idempotency-Key should return cached response and NOT double-charge")
    void testIdempotency_DuplicateRequestReturnsCachedResponse() {
        AccountResponse accA = accountService.createAccount(new CreateAccountRequest("ACC-A", new BigDecimal("100.00"), "USD"));
        AccountResponse accB = accountService.createAccount(new CreateAccountRequest("ACC-B", new BigDecimal("50.00"), "USD"));

        String idempotencyKey = UUID.randomUUID().toString();
        PaymentRequest request = new PaymentRequest(accA.id(), accB.id(), new BigDecimal("40.00"), "USD");

        // First Request
        ResponseEntity<PaymentResponse> firstResponse = transactionController.processPayment(idempotencyKey, request);
        assertThat(firstResponse.getStatusCode().value()).isEqualTo(200);

        // Balance after first payment: $100 - $40 = $60
        assertThat(accountService.getAccountEntity(accA.id()).getBalance()).isEqualByComparingTo("60.00");

        // Second Request with SAME Idempotency-Key (Simulating network retry)
        ResponseEntity<PaymentResponse> secondResponse = transactionController.processPayment(idempotencyKey, request);
        assertThat(secondResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(secondResponse.getBody().transactionReference()).isEqualTo(firstResponse.getBody().transactionReference());

        // CRITICAL CHECK: Balance must STILL be $60 (NO double charge!)
        assertThat(accountService.getAccountEntity(accA.id()).getBalance()).isEqualByComparingTo("60.00");
        assertThat(transactionRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Test 3: Same Idempotency-Key with different payload should throw 409 Conflict")
    void testIdempotency_PayloadMismatchThrowsConflict() {
        AccountResponse accA = accountService.createAccount(new CreateAccountRequest("ACC-1", new BigDecimal("100.00"), "USD"));
        AccountResponse accB = accountService.createAccount(new CreateAccountRequest("ACC-2", new BigDecimal("50.00"), "USD"));

        String idempotencyKey = UUID.randomUUID().toString();
        PaymentRequest originalRequest = new PaymentRequest(accA.id(), accB.id(), new BigDecimal("20.00"), "USD");
        transactionController.processPayment(idempotencyKey, originalRequest);

        // Same key, but changed amount to $90.00!
        PaymentRequest tamperedRequest = new PaymentRequest(accA.id(), accB.id(), new BigDecimal("90.00"), "USD");

        assertThatThrownBy(() -> transactionController.processPayment(idempotencyKey, tamperedRequest))
                .isInstanceOf(DuplicateIdempotencyKeyException.class);
    }

    @Test
    @DisplayName("Test 4: OutboxScheduler should poll PENDING records and mark them SENT")
    void testOutboxScheduler_ProcessesPendingEvents() {
        AccountResponse accA = accountService.createAccount(new CreateAccountRequest("ACC-1", new BigDecimal("100.00"), "USD"));
        AccountResponse accB = accountService.createAccount(new CreateAccountRequest("ACC-2", new BigDecimal("50.00"), "USD"));

        transactionService.executeTransfer(new PaymentRequest(accA.id(), accB.id(), new BigDecimal("25.00"), "USD"));

        List<OutboxRecord> pendingBefore = outboxRepository.findAll();
        assertThat(pendingBefore).hasSize(1);
        assertThat(pendingBefore.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);

        // Trigger background scheduler
        outboxScheduler.processOutboxEvents();

        List<OutboxRecord> processedAfter = outboxRepository.findAll();
        assertThat(processedAfter.get(0).getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(processedAfter.get(0).getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Test 5: Reconciliation should detect missing records and amount mismatches against PSP CSV")
    void testReconciliation_DetectsDiscrepancies() {
        AccountResponse accA = accountService.createAccount(new CreateAccountRequest("ACC-1", new BigDecimal("200.00"), "USD"));
        AccountResponse accB = accountService.createAccount(new CreateAccountRequest("ACC-2", new BigDecimal("50.00"), "USD"));

        PaymentResponse payment1 = transactionService.executeTransfer(new PaymentRequest(accA.id(), accB.id(), new BigDecimal("50.00"), "USD"));
        PaymentResponse payment2 = transactionService.executeTransfer(new PaymentRequest(accA.id(), accB.id(), new BigDecimal("30.00"), "USD"));

        // Simulated PSP CSV:
        // - payment1 has an AMOUNT MISMATCH ($40 instead of $50)
        // - payment2 is MISSING from PSP report
        // - GHOST-999 exists in PSP report but MISSING in our ledger
        String pspCsv = "transaction_reference,amount,currency,status\n"
                + payment1.transactionReference() + ",40.00,USD,SUCCESS\n"
                + "GHOST-999,100.00,USD,SUCCESS\n";

        InputStream csvStream = mockPspCsvGenerator.generateSampleReportCsv(pspCsv);
        List<ReconciliationError> errors = reconciliationService.reconcileDate(LocalDate.now(), csvStream);

        assertThat(errors).hasSize(3);
        assertThat(errors).extracting(ReconciliationError::getErrorType)
                .containsExactlyInAnyOrder(
                        ReconciliationErrorType.AMOUNT_MISMATCH,
                        ReconciliationErrorType.MISSING_IN_PSP,
                        ReconciliationErrorType.MISSING_IN_LEDGER
                );
    }
}