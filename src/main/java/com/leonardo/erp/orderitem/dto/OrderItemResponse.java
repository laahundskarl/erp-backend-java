package com.leonardo.erp.orderitem.dto;

import com.leonardo.erp.orderitem.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID orderId,
        UUID catalogItemId,
        String catalogItemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem entity) {
        BigDecimal subtotal = entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        return new OrderItemResponse(
                entity.getId(),
                entity.getOrder().getId(),
                entity.getCatalogItem().getId(),
                entity.getCatalogItem().getName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                subtotal);
    }
}
