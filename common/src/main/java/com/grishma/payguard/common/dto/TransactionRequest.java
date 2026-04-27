package com.grishma.payguard.common.dto;

public record TransactionRequest(
        String id,
        double amount,
        String type,
        String accountId
) {}
