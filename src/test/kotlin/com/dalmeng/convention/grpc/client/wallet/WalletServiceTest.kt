package com.dalmeng.convention.grpc.client.wallet

import com.example.wallet.grpc.DecreaseWalletResponse
import com.example.wallet.grpc.PaymentResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class WalletServiceTest : BehaviorSpec({
    val walletGrpcClient = mockk<WalletGrpcClient>()
    val walletService = WalletService(walletGrpcClient)

    Given("Decrease Balance Request") {
        val userId = "test_user_id_1"
        val orderId = "test_order_id_1"
        val amount = 1000L

        When("decrease succeeds") {
            val response = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(amount)
                .setPaymentResult(PaymentResult.SUCCEED)
                .setBeforeBalance(5000)
                .setAfterBalance(4000)
                .setSuccess(true)
                .setMessage("Payment succeeded")
                .build()

            coEvery {
                walletGrpcClient.decrease(
                    orderId = orderId,
                    userId = userId,
                    amount = amount
                )
            } returns response

            val result = walletService.decreaseBalance(userId, orderId, amount)

            Then("decrease response is returned successfully") {
                result.orderId shouldBe orderId
                result.userId shouldBe userId
                result.requestedAmount shouldBe amount
                result.paymentResult shouldBe PaymentResult.SUCCEED
                result.beforeBalance shouldBe 5000
                result.afterBalance shouldBe 4000
                result.success shouldBe true
                result.message shouldBe "Payment succeeded"
            }
        }

        When("decrease fails with insufficient balance") {
            val response = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(amount)
                .setPaymentResult(PaymentResult.INSUFFICIENT_BALANCE)
                .setBeforeBalance(500)
                .setAfterBalance(500)
                .setSuccess(false)
                .setMessage("Insufficient balance")
                .build()

            coEvery {
                walletGrpcClient.decrease(
                    orderId = orderId,
                    userId = userId,
                    amount = amount
                )
            } returns response

            val result = walletService.decreaseBalance(userId, orderId, amount)

            Then("decrease response with failure is returned") {
                result.paymentResult shouldBe PaymentResult.INSUFFICIENT_BALANCE
                result.success shouldBe false
                result.message shouldBe "Insufficient balance"
            }
        }

        When("decrease fails with invalid payment") {
            val response = DecreaseWalletResponse.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setRequestedAmount(amount)
                .setPaymentResult(PaymentResult.INVALID_PAYMENT)
                .setBeforeBalance(5000)
                .setAfterBalance(5000)
                .setSuccess(false)
                .setMessage("Invalid payment")
                .build()

            coEvery {
                walletGrpcClient.decrease(
                    orderId = orderId,
                    userId = userId,
                    amount = amount
                )
            } returns response

            val result = walletService.decreaseBalance(userId, orderId, amount)

            Then("decrease response with invalid payment is returned") {
                result.paymentResult shouldBe PaymentResult.INVALID_PAYMENT
                result.success shouldBe false
                result.message shouldBe "Invalid payment"
            }
        }
    }
})

