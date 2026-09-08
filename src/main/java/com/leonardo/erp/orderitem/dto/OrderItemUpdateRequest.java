package com.leonardo.erp.orderitem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemUpdateRequest(@NotNull @Positive Integer quantity) {
}
