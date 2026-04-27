package com.grishma.payguard.fraud.service;

import com.grishma.payguard.common.event.FraudResultEvent;
import com.grishma.payguard.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FraudRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleEngine.class);

    private static final double HIGH_AMOUNT_THRESHOLD = 50_000.0;
    private static final double MEDIUM_AMOUNT_THRESHOLD = 10_000.0;
    private static final int VELOCITY_LIMIT = 5;
    private static final String VELOCITY_PREFIX = "velocity:";

    private final RedisTemplate<String, Object> redisTemplate;

    public FraudRuleEngine(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public FraudResultEvent assess(TransactionEvent event) {
        log.info("Assessing transaction: {} | amount: {} | account: {}",
                event.transactionId(), event.amount(), event.accountId());

        // Rule 1: Amount threshold check
        if (event.amount() > HIGH_AMOUNT_THRESHOLD) {
            return blocked(event, "AMOUNT_THRESHOLD",
                    "Transaction exceeds high-value limit — blocked for review");
        }

        // Rule 2: Velocity check — too many transactions in short window
        if (isVelocityBreached(event.accountId())) {
            return blocked(event, "VELOCITY_BREACH",
                    "Too many transactions in short window — possible fraud");
        }

        // Rule 3: Medium amount flagging
        if (event.amount() > MEDIUM_AMOUNT_THRESHOLD) {
            return flagged(event, "LARGE_AMOUNT",
                    "Transaction flagged for review — amount above threshold");
        }

        // Rule 4: Safe transaction
        incrementVelocityCounter(event.accountId());
        return safe(event);
    }

    private boolean isVelocityBreached(String accountId) {
        String key = VELOCITY_PREFIX + accountId;
        Long count = redisTemplate.opsForValue().increment(key, 0);
        return count != null && count >= VELOCITY_LIMIT;
    }

    private void incrementVelocityCounter(String accountId) {
        String key = VELOCITY_PREFIX + accountId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(5));
    }

    private FraudResultEvent blocked(TransactionEvent event, String flag, String message) {
        log.warn("BLOCKED: {} | flag: {} | {}", event.transactionId(), flag, message);
        return new FraudResultEvent(
                event.transactionId(), event.accountId(), event.amount(),
                "HIGH", flag, true, message, LocalDateTime.now()
        );
    }

    private FraudResultEvent flagged(TransactionEvent event, String flag, String message) {
        log.info("FLAGGED: {} | flag: {} | {}", event.transactionId(), flag, message);
        return new FraudResultEvent(
                event.transactionId(), event.accountId(), event.amount(),
                "MEDIUM", flag, false, message, LocalDateTime.now()
        );
    }

    private FraudResultEvent safe(TransactionEvent event) {
        log.info("SAFE: {}", event.transactionId());
        return new FraudResultEvent(
                event.transactionId(), event.accountId(), event.amount(),
                "LOW", "NONE", false, "Transaction looks safe", LocalDateTime.now()
        );
    }
}
