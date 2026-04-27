package com.grishma.payguard.transaction.repository;

import com.grishma.payguard.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByAccountId(String accountId);

    List<Transaction> findByRiskLevel(String riskLevel);

    List<Transaction> findByBlocked(boolean blocked);

    List<Transaction> findByStatus(String status);

    List<Transaction> findByAccountIdAndCreatedAtBetween(String accountId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold AND t.status = 'PENDING'")
    List<Transaction> findPendingHighValueTransactions(double threshold);

    long countByAccountIdAndStatus(String accountId, String status);
}
