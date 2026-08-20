package com.idempotentpayment.transaction;

import com.idempotentpayment.account.Account;
import com.idempotentpayment.account.AccountRepository;
import com.idempotentpayment.account.AccountService;
import com.idempotentpayment.outbox.OutboxService;
import com.idempotentpayment.transaction.dto.PaymentRequest;
import com.idempotentpayment.transaction.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Transaction Service.
 * 
 * Core payment transfer engine.
 * 
 * Responsibilities:
 * 1. Orchestrates money movement between source and target accounts.
 * 2. Annotated with @Transactional to ensure atomicity across both accounts and the ledger.
 * 3. Handles Optimistic Locking: Hibernate automatically checks @Version on Account updates.
 * 4. Integrates with OutboxService to record pending notifications.
 */
@Service
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final OutboxService outboxService;

    public TransactionService(
            AccountService accountService,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            OutboxService outboxService
    ) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.outboxService = outboxService;
    }

    /**
     * Executes an atomic money transfer between two accounts.
     * 
     * If a concurrent modification occurs on either account, Hibernate throws
     * an ObjectOptimisticLockingFailureException, causing this transaction to roll back.
     */
    @Transactional
    public PaymentResponse executeTransfer(PaymentRequest request) {
        // 1. Input Validation
        if (request == null) {
            throw new IllegalArgumentException("PaymentRequest cannot be null");
        }
        if (request.sourceAccountId() == null || request.targetAccountId() == null) {
            throw new IllegalArgumentException("Source and Target Account IDs cannot be null");
        }
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new IllegalArgumentException("Source and target accounts must be distinct");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be strictly positive");
        }
        if (request.currency() == null || request.currency().length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code (e.g. USD)");
        }

        // 2. Fetch Accounts
        Account source = accountService.getAccountEntity(request.sourceAccountId());
        Account target = accountService.getAccountEntity(request.targetAccountId());

        // 3. Currency Validation
        if (!source.getCurrency().equalsIgnoreCase(request.currency()) ||
            !target.getCurrency().equalsIgnoreCase(request.currency())) {
            throw new IllegalArgumentException("Currency mismatch between accounts and transfer request");
        }

        // 4. Mutate Account Balances (Domain Logic)
        source.debit(request.amount());  // Throws InsufficientFundsException if balance < amount
        target.credit(request.amount());

        // 5. Persist Account Updates (Hibernate checks @Version during flush/commit)
        accountRepository.save(source);
        accountRepository.save(target);

        // 6. Record Immutable Ledger Entry
        String transactionReference = UUID.randomUUID().toString();
        TransactionRecord record = new TransactionRecord(
                transactionReference,
                source.getId(),
                target.getId(),
                request.amount(),
                request.currency().toUpperCase(),
                TransactionStatus.SUCCESS
        );
        TransactionRecord savedRecord = transactionRepository.save(record);

        // 7. Return PaymentResponse DTO
        return new PaymentResponse(
                savedRecord.getId(),
                savedRecord.getTransactionReference(),
                savedRecord.getSourceAccountId(),
                savedRecord.getTargetAccountId(),
                savedRecord.getAmount(),
                savedRecord.getCurrency(),
                savedRecord.getStatus(),
                savedRecord.getCreatedAt()
        );
    }
}
