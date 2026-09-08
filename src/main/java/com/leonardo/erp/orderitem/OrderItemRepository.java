package com.leonardo.erp.orderitem;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID>, OrderItemRepositoryCustom {

    boolean existsByCatalogItemId(UUID catalogItemId);
}
