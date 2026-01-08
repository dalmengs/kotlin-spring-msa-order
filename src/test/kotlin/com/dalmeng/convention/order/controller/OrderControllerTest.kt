package com.dalmeng.convention.order.controller

import com.dalmeng.convention.common.paging.BasePagingRequest
import com.dalmeng.convention.common.paging.PagingDirection
import com.dalmeng.convention.common.paging.PagingResponse
import com.dalmeng.convention.order.dto.request.CreateOrderRequest
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.dto.response.PaymentResponse
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.service.OrderService
import com.dalmeng.convention.order.service.OrderServiceFacade
import com.example.wallet.grpc.PaymentResult
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@WebMvcTest(OrderController::class)
class OrderControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean private val orderService: OrderService,
    @MockkBean private val orderServiceFacade: OrderServiceFacade,
) : BehaviorSpec({

    val objectMapper = ObjectMapper()

    Given("POST /api/order - Create Order") {
        val userId = "test_user_id_1"
        val request = CreateOrderRequest(1000)
        val order = Order.create(userId, 1000)
        val orderResponse = OrderResponse.from(order)

        When("valid request is provided") {
            every {
                orderService.createOrder(userId, request)
            } returns orderResponse

            Then("order is created successfully") {
                mockMvc.perform(
                    post("/api/order")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Succeed"))
                    .andExpect(jsonPath("$.data.orderId").value(orderResponse.orderId))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.amount").value(1000))
                    .andExpect(jsonPath("$.data.status").value("CREATED"))
                    .andExpect(jsonPath("$.data.seq").doesNotExist())
            }
        }
    }

    Given("POST /api/order/{orderId} - Handle Payment") {
        val userId = "test_user_id_1"
        val orderId = "test_order_id_1"
        val paymentResponse = PaymentResponse(
            orderId = orderId,
            aggregateId = "test_aggregate_id_1",
            userId = userId,
            requestedAmount = 1000,
            paymentResult = PaymentResult.SUCCEED,
            beforeBalance = 5000,
            afterBalance = 4000,
            success = true,
            message = "Payment succeeded"
        )

        When("valid payment request is provided") {
            every {
                orderServiceFacade.handlePayment(userId, orderId)
            } returns paymentResponse

            Then("payment is processed successfully") {
                mockMvc.perform(
                    post("/api/order/{orderId}", orderId)
                        .header("X-User-Id", userId)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Succeed"))
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.requestedAmount").value(1000))
                    .andExpect(jsonPath("$.data.paymentResult").value("SUCCEED"))
                    .andExpect(jsonPath("$.data.success").value(true))
            }
        }
    }

    Given("GET /api/order/{orderId} - Find Order by OrderId") {
        val userId = "test_user_id_1"
        val orderId = "test_order_id_1"
        val order = Order.create(userId, 1000)
        val orderResponse = OrderResponse.from(order)

        When("order exists") {
            every {
                orderService.findOrderByOrderId(userId, orderId)
            } returns orderResponse

            Then("order is returned successfully") {
                mockMvc.perform(
                    get("/api/order/{orderId}", orderId)
                        .header("X-User-Id", userId)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Succeed"))
                    .andExpect(jsonPath("$.data.orderId").value(orderResponse.orderId))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.amount").value(1000))
                    .andExpect(jsonPath("$.data.status").value("CREATED"))
                    .andExpect(jsonPath("$.data.seq").doesNotExist())
            }
        }
    }

    Given("GET /api/order - Find Orders by UserId") {
        val userId = "test_user_id_1"
        val order1 = Order.create(userId, 1000)
        val order2 = Order.create(userId, 2000)
        val orders = listOf(order1, order2)
        val orderResponses = orders.map { OrderResponse.from(it) }

        val pagingResponse = PagingResponse(
            items = orderResponses,
            page = 1,
            limit = 20,
            nextCursor = null,
            prevCursor = null,
            hasNext = false,
            hasPrev = null,
            itemCount = 2
        )

        When("valid paging request is provided") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.DOWN
            )

            every {
                orderService.findOrdersByUserId(userId, any())
            } returns pagingResponse

            Then("orders are returned successfully") {
                mockMvc.perform(
                    get("/api/order")
                        .header("X-User-Id", userId)
                        .param("limit", "20")
                        .param("page", "1")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Succeed"))
                    .andExpect(jsonPath("$.data.items.length()").value(2))
                    .andExpect(jsonPath("$.data.items[0].userId").value(userId))
                    .andExpect(jsonPath("$.data.items[1].userId").value(userId))
                    .andExpect(jsonPath("$.data.limit").value(20))
                    .andExpect(jsonPath("$.data.itemCount").value(2))
                    .andExpect(jsonPath("$.data.items[0].seq").doesNotExist())
                    .andExpect(jsonPath("$.data.items[1].seq").doesNotExist())
            }
        }

        When("cursor-based paging request is provided") {
            val cursorPagingResponse = PagingResponse(
                items = orderResponses,
                page = null,
                limit = 10,
                nextCursor = "next_cursor",
                prevCursor = null,
                hasNext = true,
                hasPrev = null,
                itemCount = 2
            )

            every {
                orderService.findOrdersByUserId(userId, any())
            } returns cursorPagingResponse

            Then("orders with cursor are returned successfully") {
                mockMvc.perform(
                    get("/api/order")
                        .header("X-User-Id", userId)
                        .param("limit", "10")
                        .param("cursor", "some_cursor")
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.nextCursor").value("next_cursor"))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.items.length()").value(2))
            }
        }
    }
})

