package com.grishma.payguard.notification.kafka;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudAlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudAlertConsumer.class);

    private final NotificationService notificationService;

    public FraudAlertConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "fraud-results", groupId = "notification-service-group")
    public void consumeFraudResult(FraudResultEvent event) {
        log.info("Received fraud result for notification: {}", event.transactionId());
        notificationService.processFraudResult(event);
    }
}
