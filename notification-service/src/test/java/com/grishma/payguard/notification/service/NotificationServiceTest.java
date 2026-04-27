package com.grishma.payguard.notification.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        notificationService = new NotificationService(emailService);
        Field alertEmailField = NotificationService.class.getDeclaredField("alertEmail");
        alertEmailField.setAccessible(true);
        alertEmailField.set(notificationService, "fraud-ops@payguard.icici.com");
    }

    @Test
    void shouldSendAlertForHighRiskTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN001", "ACC123", 60000.0, "HIGH", "AMOUNT_THRESHOLD",
                true, "Blocked", LocalDateTime.now()
        );

        notificationService.processFraudResult(event);

        verify(emailService).sendFraudAlert(eq(event), eq("fraud-ops@payguard.icici.com"));
    }

    @Test
    void shouldSendAlertForMediumRiskTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN002", "ACC456", 25000.0, "MEDIUM", "LARGE_AMOUNT",
                false, "Flagged", LocalDateTime.now()
        );

        notificationService.processFraudResult(event);

        verify(emailService).sendFraudAlert(eq(event), eq("fraud-ops@payguard.icici.com"));
    }

    @Test
    void shouldNotSendAlertForLowRiskTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN003", "ACC789", 5000.0, "LOW", "NONE",
                false, "Safe", LocalDateTime.now()
        );

        notificationService.processFraudResult(event);

        verify(emailService, never()).sendFraudAlert(any(), anyString());
    }

    @Test
    void shouldSendAlertForBlockedTransaction() {
        FraudResultEvent event = new FraudResultEvent(
                "TXN004", "ACC111", 3000.0, "HIGH", "VELOCITY_BREACH",
                true, "Velocity breach", LocalDateTime.now()
        );

        notificationService.processFraudResult(event);

        verify(emailService).sendFraudAlert(eq(event), eq("fraud-ops@payguard.icici.com"));
    }
}
