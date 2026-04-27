package com.grishma.payguard.transaction.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_account_id", columnList = "accountId"),
        @Index(name = "idx_risk_level", columnList = "riskLevel"),
        @Index(name = "idx_status", columnList = "status")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String status;

    private String riskLevel;
    private String flag;
    private boolean blocked;
    private String message;
    private LocalDateTime assessedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(String transactionId, double amount, String type, String accountId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.accountId = accountId;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void applyFraudAssessment(String riskLevel, String flag, boolean blocked, String message) {
        this.riskLevel = riskLevel;
        this.flag = flag;
        this.blocked = blocked;
        this.message = message;
        this.assessedAt = LocalDateTime.now();
        this.status = blocked ? "BLOCKED" : "APPROVED";
    }

    public String getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getAccountId() { return accountId; }
    public String getStatus() { return status; }
    public String getRiskLevel() { return riskLevel; }
    public String getFlag() { return flag; }
    public boolean isBlocked() { return blocked; }
    public String getMessage() { return message; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}
