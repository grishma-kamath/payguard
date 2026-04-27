package com.grishma.payguard.transaction.controller;

import com.grishma.payguard.common.dto.TransactionRequest;
import com.grishma.payguard.transaction.model.Transaction;
import com.grishma.payguard.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/assess")
    public ResponseEntity<Map<String, Object>> submitTransaction(@RequestBody TransactionRequest request) {
        Transaction txn = transactionService.submitTransaction(request);
        return ResponseEntity.accepted().body(Map.of(
                "status", "RECEIVED",
                "transactionId", txn.getTransactionId(),
                "message", "Transaction queued for fraud assessment"
        ));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getByTransactionId(@PathVariable String transactionId) {
        return transactionService.getByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getByAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<Transaction>> getHighRisk() {
        return ResponseEntity.ok(transactionService.getHighRiskTransactions());
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<Transaction>> getBlocked() {
        return ResponseEntity.ok(transactionService.getBlockedTransactions());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(transactionService.getByStatus(status));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Transaction Service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
