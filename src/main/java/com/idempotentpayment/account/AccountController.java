package com.idempotentpayment.account;

import com.idempotentpayment.account.dto.AccountResponse;
import com.idempotentpayment.account.dto.CreateAccountRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * Account Controller.
 * 
 * REST API entry point for managing accounts.
 * Endpoints:
 * - POST /api/v1/accounts: Create a new account
 * - GET  /api/v1/accounts/{id}: Get account details and balance
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // TODO: Implement HTTP endpoints:
    // @PostMapping -> createAccount(@RequestBody CreateAccountRequest request)
    // @GetMapping("/{id}") -> getAccount(@PathVariable Long id)
    @PostMapping("/signup")
    public ResponseEntity<AccountResponse> postMethodName(@RequestBody CreateAccountRequest request) {
        accountService.createAccount(request);
        return ResponseEntity.ok(accountService.getAccountByNumber(request.accountNumber()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse>getAccountById (@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }
    
}
