package com.grishma.payguard;

import org.springframework.stereotype.Service;

@Service
public class FraudService {

    private static final double HIGH_AMOUNT_THRESHOLD = 50000.0;
    private static final double MEDIUM_AMOUNT_THRESHOLD = 10000.0;

    public FraudAssessment assess(TransactionRequest transaction) {

        // Rule 1: Very high amount
        if (transaction.amount() > HIGH_AMOUNT_THRESHOLD) {
            return new FraudAssessment(
                    transaction.id(),
                    "HIGH",
                    "AMOUNT_THRESHOLD",
                    true,
                    "Transaction exceeds ₹50,000 limit — blocked"
            );
        }

        // Rule 2: Medium amount — flag but don't block
        if (transaction.amount() > MEDIUM_AMOUNT_THRESHOLD) {
            return new FraudAssessment(
                    transaction.id(),
                    "MEDIUM",
                    "LARGE_AMOUNT",
                    false,
                    "Transaction flagged for review — amount above ₹10,000"
            );
        }

        // Rule 3: Safe
        return new FraudAssessment(
                transaction.id(),
                "LOW",
                "NONE",
                false,
                "Transaction looks safe"
        );
    }
}