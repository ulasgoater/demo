package com.idempotentpayment.account;

import com.idempotentpayment.account.dto.AccountResponse;
import com.idempotentpayment.account.dto.CreateAccountRequest;
import com.idempotentpayment.common.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Account Service.
 * 
 * Handles business logic related to bank accounts:
 * - Creating accounts
 * - Fetching account balance and details
 * - Validating account existence
 * 
 * Service translates between external DTOs and internal Entities, orchestrating repository calls.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new bank account and immediately returns the created AccountResponse DTO.
     */
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request == null || request.accountNumber() == null || request.currency() == null) {
            throw new IllegalArgumentException("Account number and currency cannot be null");
        }
        if (accountRepository.findByAccountNumber(request.accountNumber()).isPresent()) {
            throw new IllegalArgumentException("Account with number " + request.accountNumber() + " already exists");
        }
        if (request.initialBalance() != null && request.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (request.currency().length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO code (e.g. USD, EUR)");
        }

        BigDecimal balance = request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO;
        Account account = new Account(request.accountNumber(), balance, request.currency());
        Account savedAccount = accountRepository.save(account);

        return toResponse(savedAccount);
    }

    /**
     * Fetches an account by its primary key ID and returns public AccountResponse DTO.
     */
    public AccountResponse getAccount(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + id + " does not exist"));
        return toResponse(account);
    }

    /**
     * Fetches an account by its unique account number string and returns public AccountResponse DTO.
     */
    public AccountResponse getAccountByNumber(String accountNumber) {
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " does not exist"));
        return toResponse(account);
    }

    /**
     * Fetches the raw Account entity for internal service use (e.g., in TransactionService).
     */
    public Account getAccountEntity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + id + " does not exist"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()
        );
    }
}