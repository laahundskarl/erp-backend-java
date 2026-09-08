package com.leonardo.erp.orderitem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderItemRepositoryCustom {

    Page<OrderItem> search(OrderItemFilter filter, Pageable pageable);
}
