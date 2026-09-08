package com.leonardo.erp.catalogitem;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, UUID>, CatalogItemRepositoryCustom {
}
