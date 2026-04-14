package com.grishma.payguard;

public record TransactionRequest(
        String id,
        double amount,
        String type,
        String accountId
) {}