package com.grishma.payguard;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<FraudAssessment> assess(@Valid @RequestBody TransactionRequest request) {
        validateTransactionType(request.type());
        FraudAssessment assessment = fraudService.assess(request);
        return ResponseEntity.ok(assessment);
    }

    private void validateTransactionType(String type) {
        try {
            TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validTypes = Arrays.stream(TransactionType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid transaction type: '" + type + "'. Valid types are: " + validTypes
            );
        }
    }
}
