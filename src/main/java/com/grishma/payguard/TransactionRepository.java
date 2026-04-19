package com.grishma.payguard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByRiskLevel(String riskLevel);

    List<Transaction> findByBlocked(boolean blocked);

    List<Transaction> findByAccountId(String accountId);
}

//That's it. Just 3 lines of actual code — Spring Data JPA auto-generates all the SQL for you:
//findByRiskLevel("HIGH")  →  SELECT * FROM transactions WHERE risk_level = 'HIGH'
//findByBlocked(true)      →  SELECT * FROM transactions WHERE blocked = true
//findByAccountId("ACC123") →  SELECT * FROM transactions WHERE account_id = 'ACC123'
//This is the power of Spring Data JPA — no SQL writing needed for standard queries.