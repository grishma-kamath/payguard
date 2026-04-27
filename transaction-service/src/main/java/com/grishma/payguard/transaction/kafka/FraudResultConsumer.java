package com.grishma.payguard.transaction.kafka;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudResultConsumer.class);

    private final TransactionService transactionService;

    public FraudResultConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "fraud-results", groupId = "transaction-service-group")
    public void consumeFraudResult(FraudResultEvent event) {
        log.info("Received fraud result for transaction: {} | risk: {} | blocked: {}",
                event.transactionId(), event.riskLevel(), event.blocked());
        transactionService.handleFraudResult(event);
    }
}
