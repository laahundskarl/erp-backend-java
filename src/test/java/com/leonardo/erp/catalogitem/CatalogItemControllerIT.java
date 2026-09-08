package com.leonardo.erp.catalogitem;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.leonardo.erp.catalogitem.dto.CatalogItemRequest;
import com.leonardo.erp.order.Order;
import com.leonardo.erp.order.OrderRepository;
import com.leonardo.erp.order.OrderStatus;
import com.leonardo.erp.orderitem.OrderItem;
import com.leonardo.erp.orderitem.OrderItemRepository;
import com.leonardo.erp.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CatalogItemControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void createReturns201AndPersistsTheItem() throws Exception {
        CatalogItemRequest request = new CatalogItemRequest("Keyboard", "Mechanical keyboard", ItemType.PRODUCT, new BigDecimal("250.00"), true);

        mockMvc.perform(post("/api/catalog-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Keyboard")))
                .andExpect(jsonPath("$.type", is("PRODUCT")))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createRejectsBlankNameWith400() throws Exception {
        CatalogItemRequest request = new CatalogItemRequest("", null, ItemType.SERVICE, new BigDecimal("10.00"), true);

        mockMvc.perform(post("/api/catalog-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/catalog-items/{id}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listFiltersByNameAndType() throws Exception {
        catalogItemRepository.save(CatalogItem.builder().name("Consulting Hour").type(ItemType.SERVICE).price(new BigDecimal("120.00")).active(true).build());
        catalogItemRepository.save(CatalogItem.builder().name("Consulting Package").type(ItemType.SERVICE).price(new BigDecimal("500.00")).active(true).build());
        catalogItemRepository.save(CatalogItem.builder().name("Mouse").type(ItemType.PRODUCT).price(new BigDecimal("30.00")).active(true).build());

        mockMvc.perform(get("/api/catalog-items")
                        .param("name", "consulting")
                        .param("type", "SERVICE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void updateReplacesFields() throws Exception {
        CatalogItem saved = catalogItemRepository.save(
                CatalogItem.builder().name("Old name").type(ItemType.PRODUCT).price(new BigDecimal("5.00")).active(true).build());
        CatalogItemRequest request = new CatalogItemRequest("New name", "New description", ItemType.PRODUCT, new BigDecimal("9.99"), false);

        mockMvc.perform(put("/api/catalog-items/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New name")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    void deleteReturns204WhenNotReferenced() throws Exception {
        CatalogItem saved = catalogItemRepository.save(
                CatalogItem.builder().name("Disposable").type(ItemType.PRODUCT).price(new BigDecimal("1.00")).active(true).build());

        mockMvc.perform(delete("/api/catalog-items/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns409WhenReferencedByAnOrderItem() throws Exception {
        CatalogItem saved = catalogItemRepository.save(
                CatalogItem.builder().name("In use").type(ItemType.PRODUCT).price(new BigDecimal("42.00")).active(true).build());
        Order order = orderRepository.save(new Order());
        orderItemRepository.save(OrderItem.builder().order(order).catalogItem(saved).quantity(1).unitPrice(saved.getPrice()).build());

        mockMvc.perform(delete("/api/catalog-items/{id}", saved.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }
}
