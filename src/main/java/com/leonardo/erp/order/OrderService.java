package com.leonardo.erp.order;

import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.common.exception.ResourceNotFoundException;
import com.leonardo.erp.order.dto.OrderDetailResponse;
import com.leonardo.erp.order.dto.OrderRequest;
import com.leonardo.erp.order.dto.OrderSummaryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderDetailResponse create(OrderRequest request) {
        Order order = new Order();
        order.setStatus(OrderStatus.OPEN);
        order.setDiscountPercentage(request.discountPercentage());
        order.setNotes(request.notes());
        return OrderDetailResponse.from(orderRepository.save(order));
    }

    /**
     * Returns the managed entity, for use by other services (e.g. to attach an order item).
     * Callers that only need to render a response should use {@link #getDetail(UUID)} instead.
     */
    @Transactional(readOnly = true)
    public Order getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getDetail(UUID id) {
        return OrderDetailResponse.from(getById(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> list(OrderFilter filter, Pageable pageable) {
        return orderRepository.search(filter, pageable);
    }

    public OrderDetailResponse update(UUID id, OrderRequest request) {
        Order order = getById(id);
        boolean discountChanged = order.getDiscountPercentage().compareTo(request.discountPercentage()) != 0;
        if (discountChanged && order.getStatus() == OrderStatus.CLOSED) {
            throw new BusinessRuleException(
                    "Order " + id + " is CLOSED; its discount can only be changed while the order is OPEN");
        }
        order.setDiscountPercentage(request.discountPercentage());
        order.setNotes(request.notes());
        return OrderDetailResponse.from(order);
    }

    public OrderDetailResponse updateStatus(UUID id, OrderStatus status) {
        Order order = getById(id);
        order.setStatus(status);
        return OrderDetailResponse.from(order);
    }

    public void delete(UUID id) {
        Order order = getById(id);
        orderRepository.delete(order);
    }
}
