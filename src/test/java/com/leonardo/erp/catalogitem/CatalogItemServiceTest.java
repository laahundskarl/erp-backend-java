package com.leonardo.erp.catalogitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leonardo.erp.common.exception.BusinessRuleException;
import com.leonardo.erp.common.exception.ResourceNotFoundException;
import com.leonardo.erp.orderitem.OrderItemRepository;
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
class CatalogItemServiceTest {

    @Mock
    private CatalogItemRepository catalogItemRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private CatalogItemService catalogItemService;

    private CatalogItem existing;

    @BeforeEach
    void setUp() {
        existing = CatalogItem.builder()
                .id(UUID.randomUUID())
                .name("Widget")
                .type(ItemType.PRODUCT)
                .price(new BigDecimal("9.90"))
                .active(true)
                .build();
    }

    @Test
    void deleteRejectsCatalogItemReferencedByAnOrderItem() {
        when(catalogItemRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(orderItemRepository.existsByCatalogItemId(existing.getId())).thenReturn(true);

        assertThatThrownBy(() -> catalogItemService.delete(existing.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(existing.getId().toString());

        verify(catalogItemRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesCatalogItemWhenNotReferenced() {
        when(catalogItemRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(orderItemRepository.existsByCatalogItemId(existing.getId())).thenReturn(false);

        catalogItemService.delete(existing.getId());

        verify(catalogItemRepository).delete(existing);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(catalogItemRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogItemService.getById(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOverwritesMutableFields() {
        when(catalogItemRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        var request = new com.leonardo.erp.catalogitem.dto.CatalogItemRequest(
                "Widget Pro", "Updated description", ItemType.PRODUCT, new BigDecimal("19.90"), false);

        CatalogItem updated = catalogItemService.update(existing.getId(), request);

        assertThat(updated.getName()).isEqualTo("Widget Pro");
        assertThat(updated.getPrice()).isEqualByComparingTo("19.90");
        assertThat(updated.getActive()).isFalse();
    }
}
