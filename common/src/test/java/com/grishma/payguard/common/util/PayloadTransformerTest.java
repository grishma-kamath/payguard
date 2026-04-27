package com.grishma.payguard.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayloadTransformerTest {

    @Test
    void shouldMapPayload() {
        List<String> names = List.of("alice", "bob", "charlie");
        List<String> upper = PayloadTransformer.mapPayload(names, String::toUpperCase);
        assertEquals(List.of("ALICE", "BOB", "CHARLIE"), upper);
    }

    @Test
    void shouldFilterPayload() {
        List<Integer> amounts = List.of(5000, 15000, 60000, 8000, 25000);
        List<Integer> highValue = PayloadTransformer.filterPayload(amounts, a -> a > 10000);
        assertEquals(3, highValue.size());
        assertTrue(highValue.contains(15000));
        assertTrue(highValue.contains(60000));
        assertTrue(highValue.contains(25000));
    }

    @Test
    void shouldFilterAndMap() {
        List<Integer> amounts = List.of(100, 200, 300, 400, 500);
        List<String> result = PayloadTransformer.filterAndMap(
                amounts,
                a -> a > 250,
                a -> "INR " + a
        );
        assertEquals(List.of("INR 300", "INR 400", "INR 500"), result);
    }

    @Test
    void shouldGroupBy() {
        List<String> transactions = List.of("DEBIT", "CREDIT", "DEBIT", "CREDIT", "DEBIT");
        Map<String, List<String>> grouped = PayloadTransformer.groupBy(transactions, t -> t);
        assertEquals(3, grouped.get("DEBIT").size());
        assertEquals(2, grouped.get("CREDIT").size());
    }

    @Test
    void shouldConvertToMap() {
        record Account(String id, String name) {}
        List<Account> accounts = List.of(
                new Account("A1", "Savings"),
                new Account("A2", "Current")
        );
        Map<String, String> map = PayloadTransformer.toMap(accounts, Account::id, Account::name);
        assertEquals("Savings", map.get("A1"));
        assertEquals("Current", map.get("A2"));
    }

    @Test
    void shouldFindFirst() {
        List<Integer> amounts = List.of(5000, 15000, 60000);
        Integer result = PayloadTransformer.findFirst(amounts, a -> a > 10000, 0);
        assertEquals(15000, result);
    }

    @Test
    void shouldReturnDefaultWhenNoneMatch() {
        List<Integer> amounts = List.of(100, 200, 300);
        Integer result = PayloadTransformer.findFirst(amounts, a -> a > 10000, -1);
        assertEquals(-1, result);
    }

    @Test
    void shouldCountMatching() {
        List<String> statuses = List.of("ACTIVE", "INACTIVE", "ACTIVE", "ACTIVE", "CLOSED");
        long count = PayloadTransformer.countMatching(statuses, s -> "ACTIVE".equals(s));
        assertEquals(3, count);
    }

    @Test
    void shouldSortPayload() {
        List<Integer> unsorted = List.of(50000, 10000, 30000, 5000);
        List<Integer> sorted = PayloadTransformer.sortPayload(unsorted);
        assertEquals(List.of(5000, 10000, 30000, 50000), sorted);
    }

    @Test
    void shouldFlattenNestedList() {
        List<List<String>> nested = List.of(
                List.of("TXN001", "TXN002"),
                List.of("TXN003"),
                List.of("TXN004", "TXN005")
        );
        List<String> flat = PayloadTransformer.flattenNestedList(nested);
        assertEquals(5, flat.size());
        assertEquals("TXN001", flat.get(0));
        assertEquals("TXN005", flat.get(4));
    }

    @Test
    void shouldHandleEmptyCollection() {
        List<String> empty = List.of();
        assertEquals(0, PayloadTransformer.mapPayload(empty, String::toUpperCase).size());
        assertEquals(0, PayloadTransformer.filterPayload(empty, s -> true).size());
        assertEquals(0, PayloadTransformer.countMatching(empty, s -> true));
    }
}
