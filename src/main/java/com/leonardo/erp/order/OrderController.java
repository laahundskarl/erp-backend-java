package com.leonardo.erp.order;

import com.leonardo.erp.order.dto.OrderDetailResponse;
import com.leonardo.erp.order.dto.OrderRequest;
import com.leonardo.erp.order.dto.OrderStatusUpdateRequest;
import com.leonardo.erp.order.dto.OrderSummaryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer orders, their discount and open/closed status")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDetailResponse> create(@Valid @RequestBody OrderRequest request) {
        OrderDetailResponse created = orderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public OrderDetailResponse getById(@PathVariable UUID id) {
        return orderService.getDetail(id);
    }

    @GetMapping
    public Page<OrderSummaryResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Instant createdFrom,
            @RequestParam(required = false) Instant createdTo,
            Pageable pageable) {
        OrderFilter filter = new OrderFilter(status, createdFrom, createdTo);
        return orderService.list(filter, pageable);
    }

    @PutMapping("/{id}")
    public OrderDetailResponse update(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
        return orderService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public OrderDetailResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
