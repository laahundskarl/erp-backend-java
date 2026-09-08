package com.leonardo.erp.catalogitem;

import com.leonardo.erp.catalogitem.dto.CatalogItemRequest;
import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.common.exception.ResourceNotFoundException;
import com.leonardo.erp.orderitem.OrderItemRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;
    private final OrderItemRepository orderItemRepository;

    public CatalogItem create(CatalogItemRequest request) {
        CatalogItem entity = CatalogItem.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .price(request.price())
                .active(request.active())
                .build();
        return catalogItemRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public CatalogItem getById(UUID id) {
        return catalogItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CatalogItem", id));
    }

    @Transactional(readOnly = true)
    public Page<CatalogItem> list(CatalogItemFilter filter, Pageable pageable) {
        return catalogItemRepository.search(filter, pageable);
    }

    public CatalogItem update(UUID id, CatalogItemRequest request) {
        CatalogItem entity = getById(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setType(request.type());
        entity.setPrice(request.price());
        entity.setActive(request.active());
        return entity;
    }

    public void delete(UUID id) {
        CatalogItem entity = getById(id);
        if (orderItemRepository.existsByCatalogItemId(entity.getId())) {
            throw new BusinessRuleException(
                    "Catalog item " + id + " cannot be deleted because it is referenced by at least one order item");
        }
        catalogItemRepository.delete(entity);
    }
}
