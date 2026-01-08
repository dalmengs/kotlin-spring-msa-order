package com.dalmeng.convention.grpc.server.order

import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.entity.OrderStatus
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.dalmeng.convention.order.service.OrderService
import com.example.order.grpc.FindOrderRequest
import com.example.order.grpc.FindOrderResponse
import com.example.order.grpc.OrderStatus as GrpcOrderStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class OrderGrpcServerTest : BehaviorSpec({
    val orderService = mockk<OrderService>()
    val orderGrpcServer = OrderGrpcServer(orderService)

    Given("Find Order Request") {
        val userId = "test_user_id_1"
        val orderId = "test_order_id_1"
        val order = Order.create(userId, 1000)
        val orderResponse = OrderResponse.from(order)

        When("order exists") {
            val request = FindOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .build()

            coEvery {
                orderService.findOrderByOrderId(userId, orderId)
            } returns orderResponse

            val result = runBlocking {
                orderGrpcServer.findOrder(request)
            }

            Then("order response is returned successfully") {
                result.orderId shouldBe orderResponse.orderId
                result.aggregateId shouldBe orderResponse.aggregateId
                result.userId shouldBe userId
                result.amount shouldBe 1000
                result.status shouldBe GrpcOrderStatus.CREATED
                result.exists shouldBe true
                result.message shouldBe "SUCCEED"
            }
        }

        When("order does not exist") {
            val request = FindOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .build()

            coEvery {
                orderService.findOrderByOrderId(userId, orderId)
            } throws OrderNotFoundException()

            Then("OrderNotFoundException is thrown") {
                runBlocking {
                    shouldThrow<OrderNotFoundException> {
                        orderGrpcServer.findOrder(request)
                    }
                }
            }
        }

        When("order exists with PAID status") {
            val paidOrder = Order.create(userId, 1000)
            paidOrder.markAsPaid()
            val paidOrderResponse = OrderResponse.from(paidOrder)

            val request = FindOrderRequest.newBuilder()
                .setOrderId(paidOrder.orderId)
                .setUserId(userId)
                .build()

            coEvery {
                orderService.findOrderByOrderId(userId, paidOrder.orderId)
            } returns paidOrderResponse

            val result = runBlocking {
                orderGrpcServer.findOrder(request)
            }

            Then("order response with PAID status is returned") {
                result.status shouldBe GrpcOrderStatus.PAID
                result.exists shouldBe true
                result.message shouldBe "SUCCEED"
            }
        }

        When("order exists with FAILED status") {
            val failedOrder = Order.create(userId, 1000)
            failedOrder.markAsFailed()
            val failedOrderResponse = OrderResponse.from(failedOrder)

            val request = FindOrderRequest.newBuilder()
                .setOrderId(failedOrder.orderId)
                .setUserId(userId)
                .build()

            coEvery {
                orderService.findOrderByOrderId(userId, failedOrder.orderId)
            } returns failedOrderResponse

            val result = runBlocking {
                orderGrpcServer.findOrder(request)
            }

            Then("order response with FAILED status is returned") {
                result.status shouldBe GrpcOrderStatus.FAILED
                result.exists shouldBe true
                result.message shouldBe "SUCCEED"
            }
        }
    }
})

