package com.grishma.payguard.account.repository;

import com.grishma.payguard.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCustomerNameContainingIgnoreCase(String name);

    List<Account> findByAccountType(String accountType);

    List<Account> findByStatus(String status);

    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance")
    List<Account> findHighValueAccounts(double minBalance);

    long countByAccountType(String accountType);

    long countByStatus(String status);
}
