package com.leonardo.erp.catalogitem;

import com.leonardo.erp.catalogitem.dto.CatalogItemRequest;
import com.leonardo.erp.catalogitem.dto.CatalogItemResponse;
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
@RequestMapping("/api/catalog-items")
@RequiredArgsConstructor
@Tag(name = "Catalog Items", description = "Products and services that can be added to an order")
public class CatalogItemController {

    private final CatalogItemService catalogItemService;

    @PostMapping
    public ResponseEntity<CatalogItemResponse> create(@Valid @RequestBody CatalogItemRequest request) {
        CatalogItem created = catalogItemService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(CatalogItemResponse.from(created));
    }

    @GetMapping("/{id}")
    public CatalogItemResponse getById(@PathVariable UUID id) {
        return CatalogItemResponse.from(catalogItemService.getById(id));
    }

    @GetMapping
    public Page<CatalogItemResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        CatalogItemFilter filter = new CatalogItemFilter(name, type, active);
        return catalogItemService.list(filter, pageable).map(CatalogItemResponse::from);
    }

    @PutMapping("/{id}")
    public CatalogItemResponse update(@PathVariable UUID id, @Valid @RequestBody CatalogItemRequest request) {
        return CatalogItemResponse.from(catalogItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        catalogItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
