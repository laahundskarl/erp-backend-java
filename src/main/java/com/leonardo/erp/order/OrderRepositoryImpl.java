package com.leonardo.erp.order;

import com.leonardo.erp.order.dto.OrderSummaryResponse;
import com.leonardo.erp.orderitem.QOrderItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderSummaryResponse> search(OrderFilter filter, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        BooleanBuilder predicate = buildPredicate(filter, order);

        var itemCountSubquery = JPAExpressions
                .select(orderItem.count())
                .from(orderItem)
                .where(orderItem.order.eq(order));

        List<OrderSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        OrderSummaryResponse.class,
                        order.id,
                        order.createdAt,
                        order.status,
                        order.discountPercentage,
                        order.notes,
                        itemCountSubquery))
                .from(order)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(order)
                .where(predicate)
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total == null ? 0L : total);
    }

    private BooleanBuilder buildPredicate(OrderFilter filter, QOrder order) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (filter.status() != null) {
            predicate.and(order.status.eq(filter.status()));
        }
        if (filter.createdFrom() != null) {
            predicate.and(order.createdAt.goe(filter.createdFrom()));
        }
        if (filter.createdTo() != null) {
            predicate.and(order.createdAt.loe(filter.createdTo()));
        }
        return predicate;
    }
}
