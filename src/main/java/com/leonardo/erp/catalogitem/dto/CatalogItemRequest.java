package com.leonardo.erp.catalogitem.dto;

import com.leonardo.erp.catalogitem.ItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Payload for creating or fully updating a {@link com.leonardo.erp.catalogitem.CatalogItem}.
 */
public record CatalogItemRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull ItemType type,
        @NotNull @DecimalMin(value = "0.0", message = "price must not be negative") BigDecimal price,
        @NotNull Boolean active) {
}
