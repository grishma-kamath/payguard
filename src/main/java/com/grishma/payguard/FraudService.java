package com.grishma.payguard;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FraudService {

    private static final double HIGH_AMOUNT_THRESHOLD = 50000.0;
    private static final double MEDIUM_AMOUNT_THRESHOLD = 10000.0;

    private final TransactionRepository transactionRepository;

    public FraudService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public FraudAssessment assess(TransactionRequest request) {

        // Run fraud rules
        FraudAssessment assessment;

        if (request.amount() > HIGH_AMOUNT_THRESHOLD) {
            assessment = new FraudAssessment(
                    request.id(), "HIGH", "AMOUNT_THRESHOLD", true,
                    "Transaction exceeds ₹50,000 limit — blocked"
            );
        } else if (request.amount() > MEDIUM_AMOUNT_THRESHOLD) {
            assessment = new FraudAssessment(
                    request.id(), "MEDIUM", "LARGE_AMOUNT", false,
                    "Transaction flagged for review — amount above ₹10,000"
            );
        } else {
            assessment = new FraudAssessment(
                    request.id(), "LOW", "NONE", false,
                    "Transaction looks safe"
            );
        }

        // Save to database
        Transaction transaction = new Transaction(request, assessment);
        transactionRepository.save(transaction);

        return assessment;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getHighRiskTransactions() {
        return transactionRepository.findByRiskLevel("HIGH");
    }

    public List<Transaction> getBlockedTransactions() {
        return transactionRepository.findByBlocked(true);
    }
}