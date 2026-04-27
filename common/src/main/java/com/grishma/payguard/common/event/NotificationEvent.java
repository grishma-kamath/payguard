package com.grishma.payguard.common.event;

import java.time.LocalDateTime;

public record NotificationEvent(
        String notificationId,
        String recipientEmail,
        String subject,
        String body,
        String type,
        String referenceId,
        LocalDateTime createdAt
) {}
