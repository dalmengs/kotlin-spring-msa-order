package com.dalmeng.convention.order.controller

import com.dalmeng.convention.common.paging.BasePagingRequest
import com.dalmeng.convention.common.paging.PagingResponse
import com.dalmeng.convention.common.response.BaseResponse
import com.dalmeng.convention.grpc.client.wallet.WalletGrpcClient
import com.dalmeng.convention.order.dto.request.CreateOrderRequest
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.dto.response.PaymentResponse
import com.dalmeng.convention.order.service.OrderService
import com.dalmeng.convention.order.service.OrderServiceFacade
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/order")
class OrderController(
    private val orderService: OrderService,
    private val orderServiceFacade: OrderServiceFacade
) {

    @PostMapping
    fun createOrder(
        @RequestHeader("X-User-Id") userId: String,
        @RequestBody request: CreateOrderRequest,
    ): BaseResponse<OrderResponse> {
        return BaseResponse.ok(
            data = orderService.createOrder(userId, request)
        )
    }

    @PostMapping("/{orderId}")
    fun handlePayment(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable orderId: String,
    ): BaseResponse<PaymentResponse> {
        return BaseResponse.ok(
            data = orderServiceFacade.handlePayment(userId, orderId)
        )
    }

    @GetMapping("/{orderId}")
    fun findOrderByOrderId(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable orderId: String,
    ): BaseResponse<OrderResponse> {
        return BaseResponse.ok(
            data = orderService.findOrderByOrderId(userId, orderId),
        )
    }

    @GetMapping
    fun findOrdersByUserId(
        @RequestHeader("X-User-Id") userId: String,
        @ModelAttribute request: BasePagingRequest,
    ): BaseResponse<PagingResponse<OrderResponse>> {
        return BaseResponse.ok(
            data = orderService.findOrdersByUserId(userId, request),
        )
    }
}

