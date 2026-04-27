package com.grishma.payguard.account.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits", indexes = {
        @Index(name = "idx_deposit_account", columnList = "accountId"),
        @Index(name = "idx_deposit_type", columnList = "depositType"),
        @Index(name = "idx_maturity_date", columnList = "maturityDate")
})
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String depositType;

    private int tenureMonths;
    private double interestRate;
    private double maturityAmount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime maturityDate;

    public Deposit() {}

    public Deposit(String accountId, double amount, String depositType, int tenureMonths, double interestRate) {
        this.accountId = accountId;
        this.amount = amount;
        this.depositType = depositType;
        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
        this.maturityAmount = calculateMaturityAmount(amount, interestRate, tenureMonths);
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.maturityDate = LocalDateTime.now().plusMonths(tenureMonths);
    }

    private double calculateMaturityAmount(double principal, double rate, int months) {
        double years = months / 12.0;
        return principal * Math.pow(1 + rate / 100, years);
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public double getAmount() { return amount; }
    public String getDepositType() { return depositType; }
    public int getTenureMonths() { return tenureMonths; }
    public double getInterestRate() { return interestRate; }
    public double getMaturityAmount() { return maturityAmount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getMaturityDate() { return maturityDate; }

    public void setStatus(String status) { this.status = status; }
}
