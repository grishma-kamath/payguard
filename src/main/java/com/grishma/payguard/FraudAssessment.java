package com.grishma.payguard;

public record FraudAssessment(
        String transactionId,
        String riskLevel,
        String flag,
        boolean blocked,
        String message
) {}