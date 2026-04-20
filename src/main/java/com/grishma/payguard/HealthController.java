package com.grishma.payguard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final FraudService fraudService;
    private final TransactionProducer transactionProducer;

    public HealthController(FraudService fraudService, TransactionProducer transactionProducer) {
        this.fraudService = fraudService;
        this.transactionProducer = transactionProducer;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PayGuard - AI Fraud Detection",
                "version", "1.0.0",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/transactions/assess")
    public ResponseEntity<Map<String, Object>> assess(@RequestBody TransactionRequest request) {
        transactionProducer.sendTransaction(request);
        return ResponseEntity.accepted().body(Map.of(
                "status", "RECEIVED",
                "transactionId", request.id(),
                "message", "Transaction queued for fraud assessment"
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(fraudService.getAllTransactions());
    }

    @GetMapping("/transactions/high-risk")
    public ResponseEntity<List<Transaction>> getHighRisk() {
        return ResponseEntity.ok(fraudService.getHighRiskTransactions());
    }

    @GetMapping("/transactions/blocked")
    public ResponseEntity<List<Transaction>> getBlocked() {
        return ResponseEntity.ok(fraudService.getBlockedTransactions());
    }
}