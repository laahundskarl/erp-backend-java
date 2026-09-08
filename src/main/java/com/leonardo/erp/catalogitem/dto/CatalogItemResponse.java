package com.leonardo.erp.catalogitem.dto;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.ItemType;
import java.math.BigDecimal;
import java.util.UUID;

public record CatalogItemResponse(
        UUID id,
        String name,
        String description,
        ItemType type,
        BigDecimal price,
        Boolean active) {

    public static CatalogItemResponse from(CatalogItem entity) {
        return new CatalogItemResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getPrice(),
                entity.getActive());
    }
}
