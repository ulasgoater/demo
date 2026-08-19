package com.idempotentpayment.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @GetMapping
    public Page<Account> getAllAccounts(
        // for pagination, we can use the @RequestParam annotation to get the page number and size from the request parameters.
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
         @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
       Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return accountService.getAllAccounts(pageable);
    }
    @GetMapping("/balance")
    public Double getAccountBalance(@RequestParam String id) {
        return accountService.getAccountBalance(id);
    }
    
    
}
