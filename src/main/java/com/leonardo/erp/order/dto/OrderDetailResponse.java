package com.leonardo.erp.order.dto;

import com.leonardo.erp.order.Order;
import com.leonardo.erp.orderitem.dto.OrderItemResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.leonardo.erp.order.OrderStatus;

public record OrderDetailResponse(
        UUID id,
        Instant createdAt,
        OrderStatus status,
        BigDecimal discountPercentage,
        String notes,
        List<OrderItemResponse> items,
        OrderTotals totals) {

    public static OrderDetailResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        OrderTotals totals = OrderTotals.compute(order.getItems(), order.getDiscountPercentage());
        return new OrderDetailResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getDiscountPercentage(),
                order.getNotes(),
                items,
                totals);
    }
}
