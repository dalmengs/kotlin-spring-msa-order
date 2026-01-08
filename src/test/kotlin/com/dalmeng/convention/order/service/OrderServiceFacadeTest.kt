package com.dalmeng.convention.order.service

import com.dalmeng.convention.grpc.client.wallet.WalletService
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.entity.OrderStatus
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.example.wallet.grpc.DecreaseWalletResponse
import com.example.wallet.grpc.PaymentResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OrderServiceFacadeTest : BehaviorSpec({
    val orderService = mockk<OrderService>()
    val walletService = mockk<WalletService>()

    val orderServiceFacade = OrderServiceFacade(
        orderService = orderService,
        walletService = walletService,
    )

    Given("Handle Payment Request") {
        val userId = "test_user_id_1"
        val order = Order.create(userId, 1000)
        val orderResponse = OrderResponse.from(order)
        val orderId = order.orderId

        When("order exists and payment succeeds") {
            val decreaseWalletResponse = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(1000)
                .setPaymentResult(PaymentResult.SUCCEED)
                .setBeforeBalance(5000)
                .setAfterBalance(4000)
                .setSuccess(true)
                .setMessage("Payment succeeded")
                .build()

            every {
                orderService.findCreatedOrderByOrderId(userId, orderId)
            } returns orderResponse

            every {
                walletService.decreaseBalance(
                    orderId = orderId,
                    userId = userId,
                    amount = 1000
                )
            } returns decreaseWalletResponse

            val result = orderServiceFacade.handlePayment(userId, orderId)

            Then("payment response is returned successfully") {
                result.userId shouldBe userId
                result.orderId shouldBe orderId
                result.aggregateId shouldBe orderResponse.aggregateId
                result.paymentResult shouldBe PaymentResult.SUCCEED
                result.requestedAmount shouldBe 1000
                result.beforeBalance shouldBe 5000
                result.afterBalance shouldBe 4000
                result.success shouldBe true
                result.message shouldBe "Payment succeeded"
            }
        }

        When("order does not exist") {
            every {
                orderService.findCreatedOrderByOrderId(userId, orderId)
            } throws OrderNotFoundException()

            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderServiceFacade.handlePayment(userId, orderId)
                }
            }
        }

        When("order exists but payment fails with insufficient balance") {
            val decreaseWalletResponse = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(1000)
                .setPaymentResult(PaymentResult.INSUFFICIENT_BALANCE)
                .setBeforeBalance(500)
                .setAfterBalance(500)
                .setSuccess(false)
                .setMessage("Insufficient balance")
                .build()

            every {
                orderService.findCreatedOrderByOrderId(userId, orderId)
            } returns orderResponse

            every {
                walletService.decreaseBalance(
                    orderId = orderId,
                    userId = userId,
                    amount = 1000
                )
            } returns decreaseWalletResponse

            val result = orderServiceFacade.handlePayment(userId, orderId)

            Then("payment response with failure is returned") {
                result.paymentResult shouldBe PaymentResult.INSUFFICIENT_BALANCE
                result.success shouldBe false
                result.message shouldBe "Insufficient balance"
                result.beforeBalance shouldBe 500
                result.afterBalance shouldBe 500
            }
        }

        When("order exists but payment fails with invalid payment") {
            val decreaseWalletResponse = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(1000)
                .setPaymentResult(PaymentResult.INVALID_PAYMENT)
                .setBeforeBalance(5000)
                .setAfterBalance(5000)
                .setSuccess(false)
                .setMessage("Invalid payment")
                .build()

            every {
                orderService.findCreatedOrderByOrderId(userId, orderId)
            } returns orderResponse

            every {
                walletService.decreaseBalance(
                    orderId = orderId,
                    userId = userId,
                    amount = 1000
                )
            } returns decreaseWalletResponse

            val result = orderServiceFacade.handlePayment(userId, orderId)

            Then("payment response with invalid payment is returned") {
                result.paymentResult shouldBe PaymentResult.INVALID_PAYMENT
                result.success shouldBe false
                result.message shouldBe "Invalid payment"
            }
        }
    }
})

