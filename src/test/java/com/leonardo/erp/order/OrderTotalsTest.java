package com.leonardo.erp.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.ItemType;
import com.leonardo.erp.order.dto.OrderTotals;
import com.leonardo.erp.orderitem.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTotalsTest {

    @Test
    void discountAppliesOnlyToProductItems() {
        CatalogItem product = CatalogItem.builder().type(ItemType.PRODUCT).build();
        CatalogItem service = CatalogItem.builder().type(ItemType.SERVICE).build();

        OrderItem productItem = OrderItem.builder()
                .catalogItem(product)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .build();
        OrderItem serviceItem = OrderItem.builder()
                .catalogItem(service)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        OrderTotals totals = OrderTotals.compute(List.of(productItem, serviceItem), new BigDecimal("10"));

        assertThat(totals.productsTotal()).isEqualByComparingTo("200.00");
        assertThat(totals.servicesTotal()).isEqualByComparingTo("50.00");
        assertThat(totals.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(totals.grandTotal()).isEqualByComparingTo("230.00");
    }

    @Test
    void zeroDiscountLeavesGrandTotalUnchanged() {
        CatalogItem product = CatalogItem.builder().type(ItemType.PRODUCT).build();
        OrderItem item = OrderItem.builder().catalogItem(product).quantity(3).unitPrice(new BigDecimal("10.00")).build();

        OrderTotals totals = OrderTotals.compute(List.of(item), BigDecimal.ZERO);

        assertThat(totals.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(totals.grandTotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void emptyOrderTotalsAreZero() {
        OrderTotals totals = OrderTotals.compute(List.of(), new BigDecimal("15"));

        assertThat(totals.productsTotal()).isEqualByComparingTo("0.00");
        assertThat(totals.servicesTotal()).isEqualByComparingTo("0.00");
        assertThat(totals.discountAmount()).isEqualByComparingTo("0.00");
        assertThat(totals.grandTotal()).isEqualByComparingTo("0.00");
    }
}
