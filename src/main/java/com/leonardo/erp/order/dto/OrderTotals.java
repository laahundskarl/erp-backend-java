package com.leonardo.erp.order.dto;

import com.leonardo.erp.catalogitem.ItemType;
import com.leonardo.erp.orderitem.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Order totals, always derived from the order's current items and discount — never persisted,
 * so they can never drift out of sync with the underlying items.
 *
 * <p>The discount percentage applies only to items whose catalog item type is
 * {@link ItemType#PRODUCT}; service items are always charged in full.
 */
public record OrderTotals(BigDecimal productsTotal, BigDecimal servicesTotal, BigDecimal discountAmount, BigDecimal grandTotal) {

    public static OrderTotals compute(List<OrderItem> items, BigDecimal discountPercentage) {
        BigDecimal productsTotal = sumByType(items, ItemType.PRODUCT);
        BigDecimal servicesTotal = sumByType(items, ItemType.SERVICE);
        BigDecimal discountAmount = productsTotal
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = productsTotal.subtract(discountAmount).add(servicesTotal);
        return new OrderTotals(productsTotal, servicesTotal, discountAmount, grandTotal);
    }

    private static BigDecimal sumByType(List<OrderItem> items, ItemType type) {
        return items.stream()
                .filter(item -> item.getCatalogItem().getType() == type)
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
