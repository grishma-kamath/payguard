package com.grishma.payguard.fraud.kafka;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.common.event.TransactionEvent;
import com.grishma.payguard.fraud.service.FraudRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final FraudRuleEngine fraudRuleEngine;
    private final FraudResultProducer fraudResultProducer;

    public TransactionConsumer(FraudRuleEngine fraudRuleEngine, FraudResultProducer fraudResultProducer) {
        this.fraudRuleEngine = fraudRuleEngine;
        this.fraudResultProducer = fraudResultProducer;
    }

    @KafkaListener(topics = "transactions", groupId = "fraud-detection-group")
    public void consume(TransactionEvent event) {
        log.info("Received transaction for fraud assessment: {}", event.transactionId());
        FraudResultEvent result = fraudRuleEngine.assess(event);
        fraudResultProducer.publishResult(result);
    }
}
