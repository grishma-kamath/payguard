package com.grishma.payguard;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {

    private static final String TOPIC = "transactions";
    private final KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, TransactionRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(TransactionRequest transaction) {
        kafkaTemplate.send(TOPIC, transaction.id(), transaction);
        System.out.println("Sent to Kafka: " + transaction.id());
    }
}