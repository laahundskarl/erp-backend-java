package com.leonardo.erp.order;

/**
 * An {@code OPEN} order can still have its discount changed; a {@code CLOSED} order cannot.
 */
public enum OrderStatus {
    OPEN,
    CLOSED
}
