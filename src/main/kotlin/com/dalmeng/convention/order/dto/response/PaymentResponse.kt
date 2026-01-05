package com.dalmeng.convention.order.dto.response

import com.dalmeng.convention.order.entity.OrderStatus
import com.example.wallet.grpc.PaymentResult

data class PaymentResponse(
    val orderId: String,
    val aggregateId: String,
    val userId: String,
    val requestedAmount: Long,
    val paymentResult: PaymentResult,
    val beforeBalance: Long,
    val afterBalance: Long,
    val success: Boolean,
    val message: String,
)

