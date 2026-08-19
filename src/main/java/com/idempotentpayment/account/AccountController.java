package com.idempotentpayment.account;

import com.idempotentpayment.account.dto.AccountResponse;
import com.idempotentpayment.account.dto.CreateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Controller.
 * 
 * REST API entry point for managing accounts.
 * Endpoints:
 * - POST /api/v1/accounts: Create a new account (returns 201 Created)
 * - GET  /api/v1/accounts/{id}: Get account details and balance (returns 200 OK)
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }
}
