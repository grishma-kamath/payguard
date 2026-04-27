package com.grishma.payguard.common.event;

import java.time.LocalDateTime;

public record FraudResultEvent(
        String transactionId,
        String accountId,
        double amount,
        String riskLevel,
        String flag,
        boolean blocked,
        String message,
        LocalDateTime assessedAt
) {}
