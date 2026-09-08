package com.leonardo.erp.order;

import com.leonardo.erp.order.dto.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Page<OrderSummaryResponse> search(OrderFilter filter, Pageable pageable);
}
