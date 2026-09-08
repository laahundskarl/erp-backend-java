package com.leonardo.erp.orderitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.CatalogItemRepository;
import com.leonardo.erp.catalogitem.ItemType;
import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.order.Order;
import com.leonardo.erp.order.OrderRepository;
import com.leonardo.erp.orderitem.dto.OrderItemCreateRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogItemRepository catalogItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private Order order;
    private CatalogItem catalogItem;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        catalogItem = CatalogItem.builder()
                .id(UUID.randomUUID())
                .name("Widget")
                .type(ItemType.PRODUCT)
                .price(new BigDecimal("12.50"))
                .active(true)
                .build();
    }

    @Test
    void createRejectsInactiveCatalogItem() {
        catalogItem.setActive(false);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(catalogItemRepository.findById(catalogItem.getId())).thenReturn(Optional.of(catalogItem));
        OrderItemCreateRequest request = new OrderItemCreateRequest(order.getId(), catalogItem.getId(), 2);

        assertThatThrownBy(() -> orderItemService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void createSnapshotsCurrentCatalogItemPriceAsUnitPrice() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(catalogItemRepository.findById(catalogItem.getId())).thenReturn(Optional.of(catalogItem));
        when(orderItemRepository.save(org.mockito.ArgumentMatchers.any(OrderItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        OrderItemCreateRequest request = new OrderItemCreateRequest(order.getId(), catalogItem.getId(), 3);

        var response = orderItemService.create(request);

        ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
        org.mockito.Mockito.verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.subtotal()).isEqualByComparingTo("37.50");
    }
}
