package com.leonardo.erp.orderitem;

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
public class OrderItemRepositoryImpl implements OrderItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderItem> search(OrderItemFilter filter, Pageable pageable) {
        QOrderItem orderItem = QOrderItem.orderItem;
        BooleanBuilder predicate = buildPredicate(filter, orderItem);

        List<OrderItem> content = queryFactory
                .selectFrom(orderItem)
                .join(orderItem.order).fetchJoin()
                .join(orderItem.catalogItem).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderItem.id.asc())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(orderItem)
                .where(predicate)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0L : total);
    }

    private BooleanBuilder buildPredicate(OrderItemFilter filter, QOrderItem orderItem) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (filter.orderId() != null) {
            predicate.and(orderItem.order.id.eq(filter.orderId()));
        }
        if (filter.catalogItemId() != null) {
            predicate.and(orderItem.catalogItem.id.eq(filter.catalogItemId()));
        }
        return predicate;
    }
}
