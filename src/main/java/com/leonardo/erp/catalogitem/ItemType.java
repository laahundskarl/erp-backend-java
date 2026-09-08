package com.leonardo.erp.catalogitem;

/**
 * Distinguishes a catalog item that is a physical/tangible product from one that is a service.
 * Only {@link #PRODUCT} items are eligible for the order-level discount.
 */
public enum ItemType {
    PRODUCT,
    SERVICE
}
