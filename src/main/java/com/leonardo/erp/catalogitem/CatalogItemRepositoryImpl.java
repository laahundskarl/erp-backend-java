package com.leonardo.erp.catalogitem;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CatalogItemRepositoryImpl implements CatalogItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CatalogItem> search(CatalogItemFilter filter, Pageable pageable) {
        QCatalogItem catalogItem = QCatalogItem.catalogItem;
        BooleanBuilder predicate = buildPredicate(filter, catalogItem);

        List<CatalogItem> content = queryFactory
                .selectFrom(catalogItem)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(catalogItem.name.asc())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(catalogItem)
                .where(predicate)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0L : total);
    }

    private BooleanBuilder buildPredicate(CatalogItemFilter filter, QCatalogItem catalogItem) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (filter.name() != null && !filter.name().isBlank()) {
            predicate.and(catalogItem.name.containsIgnoreCase(filter.name()));
        }
        if (filter.type() != null) {
            predicate.and(catalogItem.type.eq(filter.type()));
        }
        if (filter.active() != null) {
            predicate.and(catalogItem.active.eq(filter.active()));
        }
        return predicate;
    }
}
