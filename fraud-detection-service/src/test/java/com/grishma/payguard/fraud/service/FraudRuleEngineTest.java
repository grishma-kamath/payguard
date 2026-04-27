package com.grishma.payguard.fraud.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.common.event.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudRuleEngineTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private FraudRuleEngine fraudRuleEngine;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        fraudRuleEngine = new FraudRuleEngine(redisTemplate);
    }

    @Test
    void shouldBlockHighAmountTransaction() {
        TransactionEvent event = TransactionEvent.of("TXN001", "ACC123", 60000.0, "DEBIT");

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("HIGH", result.riskLevel());
        assertEquals("AMOUNT_THRESHOLD", result.flag());
        assertTrue(result.blocked());
        assertEquals("TXN001", result.transactionId());
    }

    @Test
    void shouldFlagMediumAmountTransaction() {
        TransactionEvent event = TransactionEvent.of("TXN002", "ACC456", 25000.0, "DEBIT");

        when(valueOperations.increment("velocity:ACC456", 0)).thenReturn(0L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("MEDIUM", result.riskLevel());
        assertEquals("LARGE_AMOUNT", result.flag());
        assertFalse(result.blocked());
    }

    @Test
    void shouldApproveSafeTransaction() {
        TransactionEvent event = TransactionEvent.of("TXN003", "ACC789", 5000.0, "CREDIT");

        when(valueOperations.increment("velocity:ACC789", 0)).thenReturn(0L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("LOW", result.riskLevel());
        assertEquals("NONE", result.flag());
        assertFalse(result.blocked());
        assertEquals("Transaction looks safe", result.message());
    }

    @Test
    void shouldBlockVelocityBreach() {
        TransactionEvent event = TransactionEvent.of("TXN004", "ACC111", 3000.0, "DEBIT");

        when(valueOperations.increment("velocity:ACC111", 0)).thenReturn(6L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("HIGH", result.riskLevel());
        assertEquals("VELOCITY_BREACH", result.flag());
        assertTrue(result.blocked());
    }

    @Test
    void shouldNotBlockBelowVelocityLimit() {
        TransactionEvent event = TransactionEvent.of("TXN005", "ACC222", 2000.0, "CREDIT");

        when(valueOperations.increment("velocity:ACC222", 0)).thenReturn(3L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("LOW", result.riskLevel());
        assertFalse(result.blocked());
    }

    @Test
    void shouldBlockAtExactVelocityLimit() {
        TransactionEvent event = TransactionEvent.of("TXN006", "ACC333", 1000.0, "DEBIT");

        when(valueOperations.increment("velocity:ACC333", 0)).thenReturn(5L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertTrue(result.blocked());
        assertEquals("VELOCITY_BREACH", result.flag());
    }

    @Test
    void shouldBlockAtExactHighThreshold() {
        TransactionEvent event = TransactionEvent.of("TXN007", "ACC444", 50001.0, "DEBIT");

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertTrue(result.blocked());
        assertEquals("AMOUNT_THRESHOLD", result.flag());
    }

    @Test
    void shouldNotBlockAtExactMediumThreshold() {
        TransactionEvent event = TransactionEvent.of("TXN008", "ACC555", 10000.0, "CREDIT");

        when(valueOperations.increment("velocity:ACC555", 0)).thenReturn(0L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertFalse(result.blocked());
        assertEquals("LOW", result.riskLevel());
    }

    @Test
    void shouldPreserveTransactionIdInResult() {
        TransactionEvent event = TransactionEvent.of("UNIQUE-TXN-ID", "ACC999", 100.0, "DEBIT");

        when(valueOperations.increment(anyString(), anyLong())).thenReturn(0L);

        FraudResultEvent result = fraudRuleEngine.assess(event);

        assertEquals("UNIQUE-TXN-ID", result.transactionId());
        assertEquals("ACC999", result.accountId());
        assertEquals(100.0, result.amount());
    }
}
