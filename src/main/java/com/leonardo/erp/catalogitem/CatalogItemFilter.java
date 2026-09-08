package com.leonardo.erp.catalogitem;

/**
 * Optional filter criteria for listing catalog items. Any field left {@code null} is ignored.
 */
public record CatalogItemFilter(String name, ItemType type, Boolean active) {
}
