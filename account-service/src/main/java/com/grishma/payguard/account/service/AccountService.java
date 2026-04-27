package com.grishma.payguard.account.service;

import com.grishma.payguard.account.model.Account;
import com.grishma.payguard.account.repository.AccountRepository;
import com.grishma.payguard.common.dto.AccountRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private static final String CACHE_PREFIX = "account:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AccountService(AccountRepository accountRepository, RedisTemplate<String, Object> redisTemplate) {
        this.accountRepository = accountRepository;
        this.redisTemplate = redisTemplate;
    }

    public Account createAccount(AccountRequest request) {
        Account account = new Account(
                request.customerName(), request.email(), request.phone(),
                request.accountType(), request.initialDeposit()
        );
        account = accountRepository.save(account);
        cacheAccount(account);
        return account;
    }

    public Optional<Account> getById(String id) {
        String cacheKey = CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Account account) {
            return Optional.of(account);
        }
        Optional<Account> account = accountRepository.findById(id);
        account.ifPresent(this::cacheAccount);
        return account;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> searchByName(String name) {
        return accountRepository.findByCustomerNameContainingIgnoreCase(name);
    }

    public List<Account> getByType(String type) {
        return accountRepository.findByAccountType(type);
    }

    public List<Account> getActiveAccounts() {
        return accountRepository.findByStatus("ACTIVE");
    }

    public Account updateAccount(String id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.setCustomerName(request.customerName());
        account.setEmail(request.email());
        account.setPhone(request.phone());
        account = accountRepository.save(account);
        cacheAccount(account);
        return account;
    }

    public Account credit(String id, double amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.credit(amount);
        account = accountRepository.save(account);
        cacheAccount(account);
        return account;
    }

    public Account debit(String id, double amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.debit(amount);
        account = accountRepository.save(account);
        cacheAccount(account);
        return account;
    }

    public void deactivateAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        account.setStatus("INACTIVE");
        accountRepository.save(account);
        evictCache(id);
    }

    private void cacheAccount(Account account) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + account.getId(), account, CACHE_TTL);
    }

    private void evictCache(String id) {
        redisTemplate.delete(CACHE_PREFIX + id);
    }
}
