package com.grishma.payguard.account.service;

import com.grishma.payguard.account.model.Deposit;
import com.grishma.payguard.account.repository.DepositRepository;
import com.grishma.payguard.common.dto.DepositRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DepositService {

    private final DepositRepository depositRepository;
    private final AccountService accountService;

    public DepositService(DepositRepository depositRepository, AccountService accountService) {
        this.depositRepository = depositRepository;
        this.accountService = accountService;
    }

    public Deposit createDeposit(DepositRequest request) {
        accountService.getById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId()));

        accountService.debit(request.accountId(), request.amount());

        Deposit deposit = new Deposit(
                request.accountId(), request.amount(), request.depositType(),
                request.tenureMonths(), request.interestRate()
        );
        return depositRepository.save(deposit);
    }

    public Optional<Deposit> getById(String id) {
        return depositRepository.findById(id);
    }

    public List<Deposit> getByAccountId(String accountId) {
        return depositRepository.findByAccountId(accountId);
    }

    public List<Deposit> getByType(String type) {
        return depositRepository.findByDepositType(type);
    }

    public List<Deposit> getActiveDeposits() {
        return depositRepository.findByStatus("ACTIVE");
    }

    public List<Deposit> getMaturingDeposits(int daysAhead) {
        LocalDateTime cutoff = LocalDateTime.now().plusDays(daysAhead);
        return depositRepository.findByMaturityDateBefore(cutoff);
    }

    public Double getTotalDeposits(String accountId) {
        Double total = depositRepository.getTotalDepositsByAccountId(accountId);
        return total != null ? total : 0.0;
    }

    public Deposit closeDeposit(String id) {
        Deposit deposit = depositRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deposit not found: " + id));

        deposit.setStatus("CLOSED");
        depositRepository.save(deposit);

        accountService.credit(deposit.getAccountId(), deposit.getMaturityAmount());
        return deposit;
    }
}
