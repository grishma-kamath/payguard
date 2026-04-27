package com.grishma.payguard.common.dto;

public record FraudAssessment(
        String transactionId,
        String riskLevel,
        String flag,
        boolean blocked,
        String message
) {}
