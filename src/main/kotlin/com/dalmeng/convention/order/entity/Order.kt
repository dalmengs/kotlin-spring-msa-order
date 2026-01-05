package com.dalmeng.convention.order.entity

import com.dalmeng.convention.common.entity.BaseEntity
import com.dalmeng.convention.common.util.Utils
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.security.SecureRandom
import java.time.LocalDateTime

@Entity
@Table(name = "userorder")
class Order(
    @Column(name = "order_id", nullable = false, unique = true, length = 26)
    val orderId: String,

    @Column(name = "aggregate_id", nullable = false, length = 255)
    var aggregateId: String,

    @Column(name = "user_id", nullable = false, length = 255)
    val userId: String,

    @Column(name = "amount", nullable = false)
    val amount: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: OrderStatus,
) : BaseEntity() {

    fun markAsPaid() {
        if (this.status == OrderStatus.PAID) {
            return // Idempotent
        }
        if (this.status != OrderStatus.CREATED) {
            throw IllegalStateException("Order status must be CREATED to mark as PAID. Current status: ${this.status}")
        }
        this.status = OrderStatus.PAID
        this.updatedAt = LocalDateTime.now()
    }

    fun markAsFailed() {
        if (this.status == OrderStatus.FAILED) {
            return // Idempotent
        }
        if (this.status != OrderStatus.CREATED) {
            throw IllegalStateException("Order status must be CREATED to mark as FAILED. Current status: ${this.status}")
        }
        this.status = OrderStatus.FAILED
        this.updatedAt = LocalDateTime.now()
    }

    companion object {
        fun create(
            userId: String,
            amount: Long,
        ): Order {
            val orderId = Utils.generateRandomId()
            return Order(
                orderId = orderId,
                aggregateId = orderId,
                userId = userId,
                amount = amount,
                status = OrderStatus.CREATED,
            )
        }
    }
}

