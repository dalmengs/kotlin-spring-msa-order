package com.dalmeng.convention.order.repository

import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.entity.OrderStatus
import com.dalmeng.convention.order.entity.QOrder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class OrderQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val order = QOrder.order

    fun findByOrderId(userId: String, orderId: String): Order? {
        return queryFactory
            .selectFrom(order)
            .where(
                order.orderId.eq(orderId)
                    .and(
                    order.userId.eq(userId)
                )
            )
            .fetchOne()
    }

    fun findCreatedOrderByOrderId(userId: String, orderId: String): Order? {
        return queryFactory
            .selectFrom(order)
            .where(
                order.orderId.eq(orderId)
                    .and(
                        order.userId.eq(userId)
                    )
                    .and(
                        order.status.eq(OrderStatus.CREATED)
                    )
            )
            .fetchOne()
    }

    fun findById(id: String): Order? {
        return queryFactory
            .selectFrom(order)
            .where(order.id.eq(id))
            .fetchOne()
    }
}

