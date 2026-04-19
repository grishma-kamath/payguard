package com.grishma.payguard;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String transactionId;
    private double amount;
    private String type;
    private String accountId;

    // Fraud assessment results
    private String riskLevel;
    private String flag;
    private boolean blocked;
    private String message;

    private LocalDateTime assessedAt;

    // Constructors
    public Transaction() {}

    public Transaction(TransactionRequest req, FraudAssessment assessment) {
        this.transactionId = req.id();
        this.amount = req.amount();
        this.type = req.type();
        this.accountId = req.accountId();
        this.riskLevel = assessment.riskLevel();
        this.flag = assessment.flag();
        this.blocked = assessment.blocked();
        this.message = assessment.message();
        this.assessedAt = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getAccountId() { return accountId; }
    public String getRiskLevel() { return riskLevel; }
    public String getFlag() { return flag; }
    public boolean isBlocked() { return blocked; }
    public String getMessage() { return message; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
}