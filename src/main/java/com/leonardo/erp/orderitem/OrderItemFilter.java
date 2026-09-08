package com.leonardo.erp.orderitem;

import java.util.UUID;

/**
 * Optional filter criteria for listing order items. Any field left {@code null} is ignored.
 */
public record OrderItemFilter(UUID orderId, UUID catalogItemId) {
}
