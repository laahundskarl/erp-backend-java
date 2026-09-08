package com.leonardo.erp.order.dto;

import com.leonardo.erp.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight row for the paginated order list. Built directly by a QueryDSL projection
 * (see {@code OrderRepositoryImpl}) using a correlated item-count subquery instead of a
 * collection fetch join, so pagination stays a plain SQL {@code LIMIT}/{@code OFFSET} on
 * the {@code orders} table rather than Hibernate paginating an in-memory list.
 */
public record OrderSummaryResponse(
        UUID id,
        Instant createdAt,
        OrderStatus status,
        BigDecimal discountPercentage,
        String notes,
        Long itemCount) {
}
