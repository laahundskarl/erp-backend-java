package com.leonardo.erp.order;

import java.time.Instant;

/**
 * Optional filter criteria for listing orders. Any field left {@code null} is ignored.
 * {@code createdFrom}/{@code createdTo} bound the {@code createdAt} timestamp (inclusive).
 */
public record OrderFilter(OrderStatus status, Instant createdFrom, Instant createdTo) {
}
