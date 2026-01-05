package com.dalmeng.convention.order.dto.response

import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.entity.OrderStatus

data class OrderResponse(
    val id: String,
    val orderId: String,
    val aggregateId: String,
    val userId: String,
    val amount: Long,
    val status: OrderStatus,
    val createdAt: java.time.LocalDateTime,
    val updatedAt: java.time.LocalDateTime,
) {
    companion object {
        fun from(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id,
                orderId = order.orderId,
                aggregateId = order.aggregateId,
                userId = order.userId,
                amount = order.amount,
                status = order.status,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
            )
        }

        fun convertToGrpcStatus(status: OrderStatus): com.example.order.grpc.OrderStatus {
            return when (status) {
                OrderStatus.CREATED -> com.example.order.grpc.OrderStatus.CREATED
                OrderStatus.PAID -> com.example.order.grpc.OrderStatus.PAID
                OrderStatus.FAILED -> com.example.order.grpc.OrderStatus.FAILED
            }
        }
    }
}

