package com.grishma.payguard.common.event;

import java.time.LocalDateTime;

public record TransactionEvent(
        String transactionId,
        String accountId,
        double amount,
        String type,
        String status,
        LocalDateTime timestamp
) {
    public static TransactionEvent of(String transactionId, String accountId, double amount, String type) {
        return new TransactionEvent(transactionId, accountId, amount, type, "PENDING", LocalDateTime.now());
    }
}
