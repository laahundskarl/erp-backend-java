package com.leonardo.erp.catalogitem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogItemRepositoryCustom {

    Page<CatalogItem> search(CatalogItemFilter filter, Pageable pageable);
}
