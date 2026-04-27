package com.grishma.payguard.common.dto;

public record NotificationRequest(
        String recipientEmail,
        String subject,
        String body,
        String type,
        String referenceId
) {}
