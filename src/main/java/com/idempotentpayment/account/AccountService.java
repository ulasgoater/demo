package com.idempotentpayment.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AccountService {
    private final AccountRepository accountRepository;
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }
    public Double getAccountBalance(String id) {
        Account account = accountRepository.findById(Integer.parseInt(id)).orElse(null);
        if (account != null) {
            return account.getBalance();
        } else {
            throw new RuntimeException("Account not found");
        }
    }
}
