package com.dalmeng.convention.order.service

import com.dalmeng.convention.common.paging.BasePagingRequest
import com.dalmeng.convention.common.paging.PagingResponse
import com.dalmeng.convention.common.paging.PagingService
import com.dalmeng.convention.grpc.client.wallet.WalletGrpcClient
import com.dalmeng.convention.order.dto.request.CreateOrderRequest
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.entity.OrderStatus
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.dalmeng.convention.order.paging.OrderPagingHandlers
import com.dalmeng.convention.order.repository.OrderQueryRepository
import com.dalmeng.convention.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderQueryRepository: OrderQueryRepository,
    private val pagingService: PagingService,
) {

    @Transactional
    fun createOrder(userId: String, request: CreateOrderRequest): OrderResponse {
        val order = com.dalmeng.convention.order.entity.Order.create(
            userId = userId,
            amount = request.amount,
        )
        val savedOrder = orderRepository.save(order)
        return OrderResponse.from(savedOrder)
    }

    @Transactional(readOnly = true)
    fun findOrderByOrderId(userId: String, orderId: String): OrderResponse {
        val order = orderQueryRepository.findByOrderId(userId, orderId)
            ?: throw OrderNotFoundException()

        return OrderResponse.from(order)
    }

    @Transactional(readOnly = true)
    fun findCreatedOrderByOrderId(userId: String, orderId: String): OrderResponse {
        val order = orderQueryRepository.findCreatedOrderByOrderId(userId, orderId)
            ?: throw OrderNotFoundException()

        return OrderResponse.from(order)
    }

    @Transactional
    fun markOrderAsPaid(userId: String, orderId: String) {
        val order = orderQueryRepository.findByOrderId(userId, orderId)
            ?: throw OrderNotFoundException()
        order.markAsPaid()
    }

    @Transactional
    fun markOrderAsFailed(userId: String, orderId: String) {
        val order = orderQueryRepository.findByOrderId(userId, orderId)
            ?: throw OrderNotFoundException()
        order.markAsFailed()
    }

    @Transactional(readOnly = true)
    fun findOrdersByUserId(userId: String, request: BasePagingRequest): PagingResponse<OrderResponse> {
        request.validate()
        val handlers = OrderPagingHandlers(
            userId = userId,
            orderRepository = orderRepository,
            orderQueryRepository = orderQueryRepository,
        )
        val pagingResult = pagingService.paginate(
            request = request,
            handlers = handlers,
        )
        return pagingService.map(pagingResult) {
            OrderResponse.from(it)
        }
    }
}

