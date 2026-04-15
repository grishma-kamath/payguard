package com.grishma.payguard;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        List<String> messages,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, List.of(message), LocalDateTime.now());
    }

    public ErrorResponse(int status, String error, List<String> messages) {
        this(status, error, messages, LocalDateTime.now());
    }
}
