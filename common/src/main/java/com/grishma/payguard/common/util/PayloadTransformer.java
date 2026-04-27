package com.grishma.payguard.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic utility methods for filtering, mapping, and transforming API payloads.
 * Part of the FSL Middleware SDK — reduces boilerplate across 250+ engineers.
 */
public final class PayloadTransformer {

    private PayloadTransformer() {}

    public static <T, R> List<R> mapPayload(Collection<T> items, Function<T, R> mapper) {
        return items.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T> List<T> filterPayload(Collection<T> items, Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> filterAndMap(Collection<T> items, Predicate<T> predicate, Function<T, R> mapper) {
        return items.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T, K> Map<K, List<T>> groupBy(Collection<T> items, Function<T, K> classifier) {
        return items.stream()
                .collect(Collectors.groupingBy(classifier));
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return items.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v2));
    }

    public static <T> T findFirst(Collection<T> items, Predicate<T> predicate, T defaultValue) {
        return items.stream()
                .filter(predicate)
                .findFirst()
                .orElse(defaultValue);
    }

    public static <T> long countMatching(Collection<T> items, Predicate<T> predicate) {
        return items.stream()
                .filter(predicate)
                .count();
    }

    public static <T extends Comparable<T>> List<T> sortPayload(Collection<T> items) {
        return items.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> flattenNestedList(Collection<? extends Collection<T>> nested) {
        return nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
