package com.leonardo.erp.orderitem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OrderItemCreateRequest(
        @NotNull UUID orderId,
        @NotNull UUID catalogItemId,
        @NotNull @Positive Integer quantity) {
}
