package com.idempotentpayment.transaction;

import com.idempotentpayment.transaction.dto.PaymentRequest;
import com.idempotentpayment.transaction.dto.PaymentResponse;
import org.springframework.stereotype.Service;

/**
 * Transaction Service.
 * 
 * Core payment transfer engine.
 * 
 * Responsibilities:
 * 1. Orchestrates money movement between source and target accounts.
 * 2. Annotated with @Transactional to ensure atomicity.
 * 3. Annotated with @Retryable to handle OptimisticLockException with exponential backoff.
 * 4. Integrates with OutboxService to record pending notifications.
 */
@Service
public class TransactionService {

    // TODO: Inject AccountRepository, TransactionRepository, OutboxService
    
    // TODO: Implement @Retryable + @Transactional transfer method:
    // public PaymentResponse executeTransfer(PaymentRequest request)
}
