package com.grishma.payguard.account.controller;

import com.grishma.payguard.account.model.Account;
import com.grishma.payguard.account.service.AccountService;
import com.grishma.payguard.common.dto.AccountRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAll() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable String id) {
        return accountService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Account>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(accountService.searchByName(name));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Account>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(accountService.getByType(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> update(@PathVariable String id, @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<Account> credit(@PathVariable String id, @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(accountService.credit(id, body.get("amount")));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<Account> debit(@PathVariable String id, @RequestBody Map<String, Double> body) {
        return ResponseEntity.ok(accountService.debit(id, body.get("amount")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Account Service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
