package com.leonardo.erp.orderitem;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.CatalogItemRepository;
import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.common.exception.ResourceNotFoundException;
import com.leonardo.erp.order.Order;
import com.leonardo.erp.order.OrderRepository;
import com.leonardo.erp.orderitem.dto.OrderItemCreateRequest;
import com.leonardo.erp.orderitem.dto.OrderItemResponse;
import com.leonardo.erp.orderitem.dto.OrderItemUpdateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final CatalogItemRepository catalogItemRepository;

    public OrderItemResponse create(OrderItemCreateRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));
        CatalogItem catalogItem = catalogItemRepository.findById(request.catalogItemId())
                .orElseThrow(() -> new ResourceNotFoundException("CatalogItem", request.catalogItemId()));

        if (!Boolean.TRUE.equals(catalogItem.getActive())) {
            throw new BusinessRuleException(
                    "Catalog item " + catalogItem.getId() + " is inactive and cannot be added to an order");
        }

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .catalogItem(catalogItem)
                .quantity(request.quantity())
                .unitPrice(catalogItem.getPrice())
                .build();
        return OrderItemResponse.from(orderItemRepository.save(orderItem));
    }

    @Transactional(readOnly = true)
    public OrderItemResponse getById(UUID id) {
        return OrderItemResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderItemResponse> list(OrderItemFilter filter, Pageable pageable) {
        return orderItemRepository.search(filter, pageable).map(OrderItemResponse::from);
    }

    public OrderItemResponse update(UUID id, OrderItemUpdateRequest request) {
        OrderItem orderItem = findEntity(id);
        orderItem.setQuantity(request.quantity());
        return OrderItemResponse.from(orderItem);
    }

    public void delete(UUID id) {
        orderItemRepository.delete(findEntity(id));
    }

    private OrderItem findEntity(UUID id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", id));
    }
}
