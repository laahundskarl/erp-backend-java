package com.leonardo.erp.orderitem;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leonardo.erp.catalogitem.CatalogItem;
import com.leonardo.erp.catalogitem.CatalogItemRepository;
import com.leonardo.erp.catalogitem.ItemType;
import com.leonardo.erp.order.Order;
import com.leonardo.erp.order.OrderRepository;
import com.leonardo.erp.orderitem.dto.OrderItemCreateRequest;
import com.leonardo.erp.orderitem.dto.OrderItemUpdateRequest;
import com.leonardo.erp.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderItemControllerIT extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void createAddsItemAndSnapshotsUnitPrice() throws Exception {
        Order order = orderRepository.save(new Order());
        CatalogItem catalogItem = catalogItemRepository.save(
                CatalogItem.builder().name("Monitor").type(ItemType.PRODUCT).price(new BigDecimal("300.00")).active(true).build());
        OrderItemCreateRequest request = new OrderItemCreateRequest(order.getId(), catalogItem.getId(), 2);

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitPrice", is(300.0)))
                .andExpect(jsonPath("$.subtotal", is(600.0)));
    }

    @Test
    void createRejectsInactiveCatalogItemWith409() throws Exception {
        Order order = orderRepository.save(new Order());
        CatalogItem catalogItem = catalogItemRepository.save(
                CatalogItem.builder().name("Discontinued").type(ItemType.PRODUCT).price(new BigDecimal("10.00")).active(false).build());
        OrderItemCreateRequest request = new OrderItemCreateRequest(order.getId(), catalogItem.getId(), 1);

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createReturns404ForUnknownOrder() throws Exception {
        CatalogItem catalogItem = catalogItemRepository.save(
                CatalogItem.builder().name("Cable").type(ItemType.PRODUCT).price(new BigDecimal("5.00")).active(true).build());
        OrderItemCreateRequest request = new OrderItemCreateRequest(UUID.randomUUID(), catalogItem.getId(), 1);

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesQuantityAndRecomputesSubtotal() throws Exception {
        Order order = orderRepository.save(new Order());
        CatalogItem catalogItem = catalogItemRepository.save(
                CatalogItem.builder().name("Desk").type(ItemType.PRODUCT).price(new BigDecimal("50.00")).active(true).build());
        OrderItem item = orderItemRepository.save(
                OrderItem.builder().order(order).catalogItem(catalogItem).quantity(1).unitPrice(catalogItem.getPrice()).build());
        OrderItemUpdateRequest request = new OrderItemUpdateRequest(4);

        mockMvc.perform(put("/api/order-items/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(4)))
                .andExpect(jsonPath("$.subtotal", is(200.0)));
    }

    @Test
    void deleteRemovesTheItem() throws Exception {
        Order order = orderRepository.save(new Order());
        CatalogItem catalogItem = catalogItemRepository.save(
                CatalogItem.builder().name("Lamp").type(ItemType.PRODUCT).price(new BigDecimal("15.00")).active(true).build());
        OrderItem item = orderItemRepository.save(
                OrderItem.builder().order(order).catalogItem(catalogItem).quantity(1).unitPrice(catalogItem.getPrice()).build());

        mockMvc.perform(delete("/api/order-items/{id}", item.getId()))
                .andExpect(status().isNoContent());
    }
}
