package com.idempotentpayment;

import com.idempotentpayment.account.Account;
import com.idempotentpayment.account.AccountRepository;
import com.idempotentpayment.account.AccountService;
import com.idempotentpayment.account.dto.AccountResponse;
import com.idempotentpayment.account.dto.CreateAccountRequest;
import com.idempotentpayment.idempotency.IdempotencyRepository;
import com.idempotentpayment.idempotency.IdempotencyService;
import com.idempotentpayment.outbox.OutboxRecord;
import com.idempotentpayment.outbox.OutboxRepository;
import com.idempotentpayment.outbox.OutboxStatus;
import com.idempotentpayment.reconciliation.ReconciliationRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PaymentLedgerIntegrationTest {
 // Autowired builds the application context and injects the required beans for testing. This allows us to test the integration of multiple components in a real Spring Boot environment.
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private IdempotencyService idempotencyService;
    @Autowired private IdempotencyRepository idempotencyRepository;
    @Autowired private OutboxRepository outboxRepository;
    @Autowired private ReconciliationRepository reconciliationRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        transactionRepository.deleteAll();
        idempotencyRepository.deleteAll();
        accountRepository.deleteAll();
        reconciliationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully transfer funds, update balances, and create ledger and outbox records")
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
}