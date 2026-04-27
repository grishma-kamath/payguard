package com.grishma.payguard.account.repository;

import com.grishma.payguard.account.model.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, String> {

    List<Deposit> findByAccountId(String accountId);

    List<Deposit> findByDepositType(String depositType);

    List<Deposit> findByStatus(String status);

    List<Deposit> findByMaturityDateBefore(LocalDateTime date);

    @Query("SELECT SUM(d.amount) FROM Deposit d WHERE d.accountId = :accountId AND d.status = 'ACTIVE'")
    Double getTotalDepositsByAccountId(String accountId);
}
