package com.grishma.payguard.notification.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;

    @Value("${notification.alert-email:fraud-ops@payguard.icici.com}")
    private String alertEmail;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void processFraudResult(FraudResultEvent event) {
        log.info("Processing fraud result notification for: {} | risk: {}", event.transactionId(), event.riskLevel());

        if (event.blocked() || "HIGH".equals(event.riskLevel())) {
            log.warn("HIGH RISK transaction detected: {} — sending immediate alert", event.transactionId());
            emailService.sendFraudAlert(event, alertEmail);
        } else if ("MEDIUM".equals(event.riskLevel())) {
            log.info("MEDIUM RISK transaction: {} — flagged for review", event.transactionId());
            emailService.sendFraudAlert(event, alertEmail);
        } else {
            log.debug("LOW RISK transaction: {} — no alert needed", event.transactionId());
        }
    }
}
