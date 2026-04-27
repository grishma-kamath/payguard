package com.grishma.payguard.common.dto;

public record DepositRequest(
        String accountId,
        double amount,
        String depositType,
        int tenureMonths,
        double interestRate
) {}
