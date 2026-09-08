package com.leonardo.erp.orderitem;

import com.leonardo.erp.orderitem.dto.OrderItemCreateRequest;
import com.leonardo.erp.orderitem.dto.OrderItemResponse;
import com.leonardo.erp.orderitem.dto.OrderItemUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Items", description = "Line items attached to an order")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponse> create(@Valid @RequestBody OrderItemCreateRequest request) {
        OrderItemResponse created = orderItemService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public OrderItemResponse getById(@PathVariable UUID id) {
        return orderItemService.getById(id);
    }

    @GetMapping
    public Page<OrderItemResponse> list(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID catalogItemId,
            Pageable pageable) {
        OrderItemFilter filter = new OrderItemFilter(orderId, catalogItemId);
        return orderItemService.list(filter, pageable);
    }

    @PutMapping("/{id}")
    public OrderItemResponse update(@PathVariable UUID id, @Valid @RequestBody OrderItemUpdateRequest request) {
        return orderItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
