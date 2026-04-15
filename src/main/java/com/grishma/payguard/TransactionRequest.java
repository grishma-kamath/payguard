package com.grishma.payguard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotBlank(message = "Transaction id is required")
        String id,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount,

        @NotBlank(message = "Transaction type is required")
        String type,

        @NotBlank(message = "Account id is required")
        String accountId
) {}
