package com.grishma.payguard.common.dto;

public record AccountRequest(
        String customerName,
        String email,
        String phone,
        String accountType,
        double initialDeposit
) {}
