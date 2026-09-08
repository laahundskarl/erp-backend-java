package com.leonardo.erp.order.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload for creating an order or updating its discount/notes. A newly created order is
 * always {@code OPEN}; use {@code PATCH /api/orders/{id}/status} to close or reopen it.
 */
public record OrderRequest(
        @NotNull @DecimalMin(value = "0.0", message = "discountPercentage must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "discountPercentage must be between 0 and 100")
        BigDecimal discountPercentage,
        @Size(max = 1000) String notes) {
}
