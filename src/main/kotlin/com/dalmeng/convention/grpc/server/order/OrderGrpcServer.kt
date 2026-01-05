package com.dalmeng.convention.grpc.server.order

import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.service.OrderService
import com.example.order.grpc.FindOrderRequest
import com.example.order.grpc.FindOrderResponse
import com.example.order.grpc.OrderServiceGrpcKt
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class OrderGrpcServer(
    private val orderService: OrderService
) : OrderServiceGrpcKt.OrderServiceCoroutineImplBase() {

    override suspend fun findOrder(
        request: FindOrderRequest
    ): FindOrderResponse {
        val orderResponse = orderService.findOrderByOrderId(
            userId = request.userId,
            orderId = request.orderId
        )

        return FindOrderResponse.newBuilder()
            .setOrderId(orderResponse.orderId)
            .setAggregateId(orderResponse.aggregateId)
            .setUserId(request.userId)
            .setAmount(orderResponse.amount)
            .setStatus(OrderResponse.Companion.convertToGrpcStatus(orderResponse.status))
            .setExists(true)
            .setMessage("SUCCEED")
            .build()
    }
}