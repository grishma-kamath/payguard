package com.grishma.payguard.fraud.kafka;

import com.grishma.payguard.common.event.FraudResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FraudResultProducer {

    private static final Logger log = LoggerFactory.getLogger(FraudResultProducer.class);
    private static final String TOPIC = "fraud-results";

    private final KafkaTemplate<String, FraudResultEvent> kafkaTemplate;

    public FraudResultProducer(KafkaTemplate<String, FraudResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishResult(FraudResultEvent event) {
        kafkaTemplate.send(TOPIC, event.transactionId(), event);
        log.info("Published fraud result for: {} | risk: {}", event.transactionId(), event.riskLevel());
    }
}
