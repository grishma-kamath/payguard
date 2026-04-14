package com.grishma.payguard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final FraudService fraudService;

    public HealthController(FraudService fraudService) {
        this.fraudService = fraudService;
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
    public ResponseEntity<FraudAssessment> assess(@RequestBody TransactionRequest request) {
        FraudAssessment assessment = fraudService.assess(request);
        return ResponseEntity.ok(assessment);
    }
}