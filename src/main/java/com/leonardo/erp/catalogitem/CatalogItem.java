package com.leonardo.erp.catalogitem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.UuidGenerator;

/**
 * A sellable catalog entry: either a physical product or a service.
 * {@link #active} controls whether it can still be added to new orders;
 * existing order items are unaffected by deactivation.
 */
@Entity
@Table(name = "catalog_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @NotNull
    @DecimalMin(value = "0.0", message = "price must not be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
