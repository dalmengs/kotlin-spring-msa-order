package com.dalmeng.convention.order.service

import com.dalmeng.convention.common.paging.BasePagingRequest
import com.dalmeng.convention.common.paging.PagingResponse
import com.dalmeng.convention.common.paging.PagingService
import com.dalmeng.convention.grpc.client.wallet.WalletGrpcClient
import com.dalmeng.convention.grpc.client.wallet.WalletService
import com.dalmeng.convention.order.dto.request.CreateOrderRequest
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.dto.response.PaymentResponse
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.dalmeng.convention.order.paging.OrderPagingHandlers
import com.dalmeng.convention.order.repository.OrderQueryRepository
import com.dalmeng.convention.order.repository.OrderRepository
import com.example.wallet.grpc.DecreaseWalletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderServiceFacade(
    private val orderService: OrderService,
    private val walletService: WalletService
) {
    fun handlePayment(userId: String, orderId: String): PaymentResponse {
        val orderResponse = orderService.findCreatedOrderByOrderId(userId, orderId)

        val decreaseWalletResponse = walletService.decreaseBalance(
            orderId = orderResponse.orderId,
            userId = userId,
            amount = orderResponse.amount,
        )

        return PaymentResponse(
            userId = userId,
            orderId = decreaseWalletResponse.orderId,
            aggregateId = orderResponse.aggregateId,
            paymentResult = decreaseWalletResponse.paymentResult,
            requestedAmount = decreaseWalletResponse.requestedAmount,
            beforeBalance = decreaseWalletResponse.beforeBalance,
            afterBalance = decreaseWalletResponse.afterBalance,
            success = decreaseWalletResponse.success,
            message = decreaseWalletResponse.message,
        )
    }

}

