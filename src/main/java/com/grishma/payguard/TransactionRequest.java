package com.grishma.payguard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotBlank(message = "Transaction ID is required")
        String id,

        @Positive(message = "Amount must be positive")
        double amount,

        @NotBlank(message = "Transaction type is required")
        String type,

        @NotBlank(message = "Account ID is required")
        String accountId
) {}
