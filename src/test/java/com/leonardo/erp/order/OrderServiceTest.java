package com.leonardo.erp.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.order.dto.OrderRequest;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.OPEN);
        order.setDiscountPercentage(new BigDecimal("5.00"));
    }

    @Test
    void updateRejectsDiscountChangeOnClosedOrder() {
        order.setStatus(OrderStatus.CLOSED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderRequest request = new OrderRequest(new BigDecimal("15.00"), "trying to sneak a discount in");

        assertThatThrownBy(() -> orderService.update(order.getId(), request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CLOSED");
    }

    @Test
    void updateAllowsNonDiscountChangesOnClosedOrder() {
        order.setStatus(OrderStatus.CLOSED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderRequest request = new OrderRequest(order.getDiscountPercentage(), "just updating notes");

        var response = orderService.update(order.getId(), request);

        assertThat(response.notes()).isEqualTo("just updating notes");
    }

    @Test
    void updateAllowsDiscountChangeOnOpenOrder() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderRequest request = new OrderRequest(new BigDecimal("25.00"), "seasonal promo");

        var response = orderService.update(order.getId(), request);

        assertThat(response.discountPercentage()).isEqualByComparingTo("25.00");
    }
}
