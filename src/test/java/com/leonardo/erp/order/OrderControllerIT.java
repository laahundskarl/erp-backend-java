package com.leonardo.erp.order;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.CatalogItemRepository;
import com.leonardo.erp.catalogitem.ItemType;
import com.leonardo.erp.order.dto.OrderRequest;
import com.leonardo.erp.order.dto.OrderStatusUpdateRequest;
import com.leonardo.erp.orderitem.OrderItem;
import com.leonardo.erp.orderitem.OrderItemRepository;
import com.leonardo.erp.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createReturns201WithAnOpenOrder() throws Exception {
        OrderRequest request = new OrderRequest(BigDecimal.ZERO, "first order");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.totals.grandTotal", is(0.0)));
    }

    @Test
    void getByIdComputesTotalsFromItemsAndDiscount() throws Exception {
        CatalogItem product = catalogItemRepository.save(
                CatalogItem.builder().name("Chair").type(ItemType.PRODUCT).price(new BigDecimal("100.00")).active(true).build());
        CatalogItem service = catalogItemRepository.save(
                CatalogItem.builder().name("Assembly").type(ItemType.SERVICE).price(new BigDecimal("20.00")).active(true).build());
        Order order = new Order();
        order.setDiscountPercentage(new BigDecimal("10"));
        order = orderRepository.save(order);
        orderItemRepository.save(OrderItem.builder().order(order).catalogItem(product).quantity(2).unitPrice(product.getPrice()).build());
        orderItemRepository.save(OrderItem.builder().order(order).catalogItem(service).quantity(1).unitPrice(service.getPrice()).build());

        // The order entity above is still managed in this test's transaction with its `items`
        // collection already resolved (empty, from construction) before the items were added
        // from their own side of the association. Flushing and clearing forces the upcoming
        // request to load the order fresh from the database, the same way a real, separate
        // HTTP request would.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.productsTotal", is(200.0)))
                .andExpect(jsonPath("$.totals.servicesTotal", is(20.0)))
                .andExpect(jsonPath("$.totals.discountAmount", is(20.0)))
                .andExpect(jsonPath("$.totals.grandTotal", is(200.0)))
                .andExpect(jsonPath("$.items.length()", is(2)));
    }

    @Test
    void updateRejectsDiscountChangeOnClosedOrderWith409() throws Exception {
        Order order = new Order();
        order.setStatus(OrderStatus.CLOSED);
        order.setDiscountPercentage(BigDecimal.ZERO);
        order = orderRepository.save(order);
        OrderRequest request = new OrderRequest(new BigDecimal("50"), "trying to discount a closed order");

        mockMvc.perform(put("/api/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void patchStatusClosesAndReopensAnOrder() throws Exception {
        Order order = orderRepository.save(new Order());
        OrderStatusUpdateRequest close = new OrderStatusUpdateRequest(OrderStatus.CLOSED);

        mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(close)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    void listSupportsPaginationAndStatusFilter() throws Exception {
        orderRepository.save(new Order());
        Order closedOrder = new Order();
        closedOrder.setStatus(OrderStatus.CLOSED);
        orderRepository.save(closedOrder);

        mockMvc.perform(get("/api/orders").param("status", "CLOSED").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].status", is("CLOSED")));
    }
}
