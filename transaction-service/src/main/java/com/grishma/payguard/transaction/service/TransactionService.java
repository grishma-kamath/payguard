package com.grishma.payguard.transaction.service;

import com.grishma.payguard.common.dto.TransactionRequest;
import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.transaction.kafka.TransactionProducer;
import com.grishma.payguard.transaction.model.Transaction;
import com.grishma.payguard.transaction.repository.TransactionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private static final String CACHE_PREFIX = "txn:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionProducer transactionProducer,
                              RedisTemplate<String, Object> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.transactionProducer = transactionProducer;
        this.redisTemplate = redisTemplate;
    }

    public Transaction submitTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.id(), request.amount(), request.type(), request.accountId()
        );
        transaction = transactionRepository.save(transaction);
        transactionProducer.publishTransaction(request);
        cacheTransaction(transaction);
        return transaction;
    }

    public void handleFraudResult(FraudResultEvent event) {
        Optional<Transaction> optTxn = transactionRepository.findByTransactionId(event.transactionId());
        optTxn.ifPresent(txn -> {
            txn.applyFraudAssessment(event.riskLevel(), event.flag(), event.blocked(), event.message());
            transactionRepository.save(txn);
            cacheTransaction(txn);
        });
    }

    public Optional<Transaction> getByTransactionId(String transactionId) {
        String cacheKey = CACHE_PREFIX + transactionId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Transaction txn) {
            return Optional.of(txn);
        }

        Optional<Transaction> txn = transactionRepository.findByTransactionId(transactionId);
        txn.ifPresent(this::cacheTransaction);
        return txn;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public List<Transaction> getHighRiskTransactions() {
        return transactionRepository.findByRiskLevel("HIGH");
    }

    public List<Transaction> getBlockedTransactions() {
        return transactionRepository.findByBlocked(true);
    }

    public List<Transaction> getByStatus(String status) {
        return transactionRepository.findByStatus(status);
    }

    public List<Transaction> getByAccountAndDateRange(String accountId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByAccountIdAndCreatedAtBetween(accountId, start, end);
    }

    public long countByAccountAndStatus(String accountId, String status) {
        return transactionRepository.countByAccountIdAndStatus(accountId, status);
    }

    private void cacheTransaction(Transaction transaction) {
        String cacheKey = CACHE_PREFIX + transaction.getTransactionId();
        redisTemplate.opsForValue().set(cacheKey, transaction, CACHE_TTL);
    }
}
