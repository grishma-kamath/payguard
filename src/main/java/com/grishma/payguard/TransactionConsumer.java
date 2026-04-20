package com.grishma.payguard;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    private final FraudService fraudService;

    public TransactionConsumer(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @KafkaListener(topics = "transactions", groupId = "payguard-group")
    public void consume(TransactionRequest transaction) {
        System.out.println("Received from Kafka: " + transaction.id());
        fraudService.assess(transaction);
    }
}