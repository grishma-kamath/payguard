package com.grishma.payguard.transaction.service;

import com.grishma.payguard.common.dto.TransactionRequest;
import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.transaction.kafka.TransactionProducer;
import com.grishma.payguard.transaction.model.Transaction;
import com.grishma.payguard.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionProducer transactionProducer;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSubmitTransaction() {
        TransactionRequest request = new TransactionRequest("TXN001", 5000.0, "DEBIT", "ACC123");
        Transaction saved = new Transaction("TXN001", 5000.0, "DEBIT", "ACC123");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = transactionService.submitTransaction(request);

        assertNotNull(result);
        assertEquals("TXN001", result.getTransactionId());
        assertEquals(5000.0, result.getAmount());
        assertEquals("DEBIT", result.getType());
        assertEquals("ACC123", result.getAccountId());
        assertEquals("PENDING", result.getStatus());

        verify(transactionProducer).publishTransaction(request);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldHandleFraudResult() {
        Transaction txn = new Transaction("TXN001", 60000.0, "DEBIT", "ACC123");
        FraudResultEvent event = new FraudResultEvent(
                "TXN001", "ACC123", 60000.0, "HIGH", "AMOUNT_THRESHOLD",
                true, "Blocked", LocalDateTime.now()
        );

        when(transactionRepository.findByTransactionId("TXN001")).thenReturn(Optional.of(txn));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(txn);

        transactionService.handleFraudResult(event);

        verify(transactionRepository).save(txn);
        assertEquals("HIGH", txn.getRiskLevel());
        assertTrue(txn.isBlocked());
        assertEquals("BLOCKED", txn.getStatus());
    }

    @Test
    void shouldGetTransactionFromCacheFirst() {
        Transaction cached = new Transaction("TXN001", 5000.0, "DEBIT", "ACC123");
        when(valueOperations.get("txn:TXN001")).thenReturn(cached);

        Optional<Transaction> result = transactionService.getByTransactionId("TXN001");

        assertTrue(result.isPresent());
        assertEquals("TXN001", result.get().getTransactionId());
        verify(transactionRepository, never()).findByTransactionId(anyString());
    }

    @Test
    void shouldFallbackToDbWhenCacheMiss() {
        Transaction dbTxn = new Transaction("TXN002", 15000.0, "CREDIT", "ACC456");
        when(valueOperations.get("txn:TXN002")).thenReturn(null);
        when(transactionRepository.findByTransactionId("TXN002")).thenReturn(Optional.of(dbTxn));

        Optional<Transaction> result = transactionService.getByTransactionId("TXN002");

        assertTrue(result.isPresent());
        assertEquals("TXN002", result.get().getTransactionId());
        verify(transactionRepository).findByTransactionId("TXN002");
    }

    @Test
    void shouldReturnEmptyForMissingTransaction() {
        when(valueOperations.get("txn:TXN999")).thenReturn(null);
        when(transactionRepository.findByTransactionId("TXN999")).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getByTransactionId("TXN999");

        assertFalse(result.isPresent());
    }

    @Test
    void shouldGetAllTransactions() {
        List<Transaction> txns = List.of(
                new Transaction("TXN001", 5000.0, "DEBIT", "ACC123"),
                new Transaction("TXN002", 15000.0, "CREDIT", "ACC456")
        );
        when(transactionRepository.findAll()).thenReturn(txns);

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(2, result.size());
    }

    @Test
    void shouldGetHighRiskTransactions() {
        Transaction highRisk = new Transaction("TXN003", 60000.0, "DEBIT", "ACC789");
        highRisk.applyFraudAssessment("HIGH", "AMOUNT_THRESHOLD", true, "Blocked");
        when(transactionRepository.findByRiskLevel("HIGH")).thenReturn(List.of(highRisk));

        List<Transaction> result = transactionService.getHighRiskTransactions();

        assertEquals(1, result.size());
        assertEquals("HIGH", result.get(0).getRiskLevel());
    }

    @Test
    void shouldGetBlockedTransactions() {
        Transaction blocked = new Transaction("TXN003", 60000.0, "DEBIT", "ACC789");
        blocked.applyFraudAssessment("HIGH", "AMOUNT_THRESHOLD", true, "Blocked");
        when(transactionRepository.findByBlocked(true)).thenReturn(List.of(blocked));

        List<Transaction> result = transactionService.getBlockedTransactions();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isBlocked());
    }

    @Test
    void shouldGetByAccountId() {
        List<Transaction> txns = List.of(
                new Transaction("TXN001", 5000.0, "DEBIT", "ACC123"),
                new Transaction("TXN004", 3000.0, "CREDIT", "ACC123")
        );
        when(transactionRepository.findByAccountId("ACC123")).thenReturn(txns);

        List<Transaction> result = transactionService.getByAccountId("ACC123");

        assertEquals(2, result.size());
    }

    @Test
    void shouldCountByAccountAndStatus() {
        when(transactionRepository.countByAccountIdAndStatus("ACC123", "APPROVED")).thenReturn(5L);

        long count = transactionService.countByAccountAndStatus("ACC123", "APPROVED");

        assertEquals(5L, count);
    }
}
