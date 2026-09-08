package com.leonardo.erp.order.dto;

import com.leonardo.erp.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {
}
