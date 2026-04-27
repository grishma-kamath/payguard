package com.grishma.payguard.transaction.kafka;

import com.grishma.payguard.common.dto.TransactionRequest;
import com.grishma.payguard.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionProducer.class);
    private static final String TOPIC = "transactions";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransaction(TransactionRequest request) {
        TransactionEvent event = TransactionEvent.of(
                request.id(), request.accountId(), request.amount(), request.type()
        );
        kafkaTemplate.send(TOPIC, request.id(), event);
        log.info("Published transaction event: {}", request.id());
    }
}
