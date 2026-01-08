package com.dalmeng.convention.order.service

import com.dalmeng.convention.common.paging.BasePagingRequest
import com.dalmeng.convention.common.paging.PagingDirection
import com.dalmeng.convention.common.paging.PagingResponse
import com.dalmeng.convention.common.paging.PagingService
import com.dalmeng.convention.order.dto.request.CreateOrderRequest
import com.dalmeng.convention.order.dto.response.OrderResponse
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.entity.OrderStatus
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.dalmeng.convention.order.repository.OrderQueryRepository
import com.dalmeng.convention.order.repository.OrderRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class OrderServiceTest : BehaviorSpec({
    val orderRepository = mockk<OrderRepository>()
    val orderQueryRepository = mockk<OrderQueryRepository>()
    val pagingService = mockk<PagingService>()

    val orderService = OrderService(
        orderRepository,
        orderQueryRepository,
        pagingService,
    )

    Given("Create Order Request") {
        every {
            orderRepository.save(any())
        } answers { firstArg() }

        When("amount is equal to 1") {
            val request = CreateOrderRequest(1)

            val result = orderService.createOrder("test_user_id_1", request)

            Then("Order is created successfully") {
                result.amount shouldBe 1
                result.userId shouldBe "test_user_id_1"
                result.orderId shouldBe result.aggregateId
                result.status shouldBe OrderStatus.CREATED
            }
        }

        When("amount is 10000") {
            val request = CreateOrderRequest(10000)

            val result = orderService.createOrder("test_user_id_2", request)

            Then("Order is created successfully") {
                result.amount shouldBe 10000
                result.userId shouldBe "test_user_id_2"
                result.orderId shouldBe result.aggregateId
                result.status shouldBe OrderStatus.CREATED
            }
        }

        When("amount is negative") {
            val request = CreateOrderRequest(-1)

            Then("IllegalArgumentException is thrown") {
                shouldThrow<IllegalArgumentException> {
                    orderService.createOrder("test_user_id_3", request)
                }
            }
        }

        When("amount exceeds maximum limit") {
            val request = CreateOrderRequest(1000001)

            Then("IllegalArgumentException is thrown") {
                shouldThrow<IllegalArgumentException> {
                    orderService.createOrder("test_user_id_4", request)
                }
            }
        }
    }

    Given("Find Order by OrderId Request") {
        val orderEntity = Order.create("test_user_id_1", 1)
        val orderEntityId = orderEntity.orderId

        every {
            orderQueryRepository.findByOrderId(any(), any())
        } answers {
            val userId = firstArg<String>()
            val orderId = secondArg<String>()

            when {
                userId == "test_user_id_1" && orderId == orderEntityId -> orderEntity
                else -> null
            }
        }

        When("order exists for the user") {
            val order = orderService.findOrderByOrderId("test_user_id_1", orderEntityId)

            Then("order is found successfully") {
                order.orderId shouldBe orderEntityId
                order.userId shouldBe orderEntity.userId
                order.amount shouldBe 1
            }
        }

        When("order does not exist for the user") {
            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.findOrderByOrderId("test_user_id_2", orderEntityId)
                }
            }
        }

        When("order does not exist") {
            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.findOrderByOrderId("test_user_id_1", "not_exists_order_id")
                }
            }
        }

        When("both user and order do not exist") {
            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.findOrderByOrderId("test_user_id_2", "not_exists_order_id")
                }
            }
        }
    }

    Given("Find Created Order by OrderId Request") {
        val orderEntity = Order.create("test_user_id_1", 1)
        val orderEntityId = orderEntity.orderId

        every {
            orderQueryRepository.findCreatedOrderByOrderId(any(), any())
        } answers {
            val userId = firstArg<String>()
            val orderId = secondArg<String>()

            when {
                userId == "test_user_id_1" && orderId == orderEntityId -> orderEntity
                else -> null
            }
        }

        When("created order exists for the user") {
            val order = orderService.findCreatedOrderByOrderId("test_user_id_1", orderEntityId)

            Then("order is found successfully") {
                order.orderId shouldBe orderEntityId
                order.userId shouldBe orderEntity.userId
                order.amount shouldBe 1
                order.status shouldBe OrderStatus.CREATED
            }
        }

        When("created order does not exist for the user") {
            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.findCreatedOrderByOrderId("test_user_id_2", orderEntityId)
                }
            }
        }

        When("created order does not exist") {
            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.findCreatedOrderByOrderId("test_user_id_1", "not_exists_order_id")
                }
            }
        }
    }

    Given("Mark Order as Paid Request") {
        When("order exists and status is CREATED") {
            val orderEntity = Order.create("test_user_id_1", 1)
            val orderEntityId = orderEntity.orderId

            every {
                orderQueryRepository.findByOrderId("test_user_id_1", orderEntityId)
            } returns orderEntity

            orderService.markOrderAsPaid("test_user_id_1", orderEntityId)

            Then("order status is changed to PAID") {
                orderEntity.status shouldBe OrderStatus.PAID
            }
        }

        When("order does not exist") {
            val orderEntityId = "non_existent_order_id"

            every {
                orderQueryRepository.findByOrderId(any(), any())
            } returns null

            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.markOrderAsPaid("test_user_id_2", orderEntityId)
                }
            }
        }

        When("order status is already PAID") {
            val paidOrder = Order.create("test_user_id_1", 1)
            paidOrder.markAsPaid()
            val paidOrderId = paidOrder.orderId

            every {
                orderQueryRepository.findByOrderId("test_user_id_1", paidOrderId)
            } returns paidOrder

            Then("operation is idempotent and no exception is thrown") {
                orderService.markOrderAsPaid("test_user_id_1", paidOrderId)
                paidOrder.status shouldBe OrderStatus.PAID
            }
        }

        When("order status is FAILED") {
            val failedOrder = Order.create("test_user_id_3", 1)
            failedOrder.markAsFailed()

            every {
                orderQueryRepository.findByOrderId("test_user_id_3", failedOrder.orderId)
            } returns failedOrder

            Then("IllegalStateException is thrown") {
                shouldThrow<IllegalStateException> {
                    orderService.markOrderAsPaid("test_user_id_3", failedOrder.orderId)
                }
            }
        }
    }

    Given("Mark Order as Failed Request") {
        When("order exists and status is CREATED") {
            val orderEntity = Order.create("test_user_id_1", 1)
            val orderEntityId = orderEntity.orderId

            every {
                orderQueryRepository.findByOrderId("test_user_id_1", orderEntityId)
            } returns orderEntity

            orderService.markOrderAsFailed("test_user_id_1", orderEntityId)

            Then("order status is changed to FAILED") {
                orderEntity.status shouldBe OrderStatus.FAILED
            }
        }

        When("order does not exist") {
            val orderEntityId = "non_existent_order_id"

            every {
                orderQueryRepository.findByOrderId(any(), any())
            } returns null

            Then("OrderNotFoundException is thrown") {
                shouldThrow<OrderNotFoundException> {
                    orderService.markOrderAsFailed("test_user_id_2", orderEntityId)
                }
            }
        }

        When("order status is already FAILED") {
            val failedOrder = Order.create("test_user_id_1", 1)
            failedOrder.markAsFailed()
            val failedOrderId = failedOrder.orderId

            every {
                orderQueryRepository.findByOrderId("test_user_id_1", failedOrderId)
            } returns failedOrder

            Then("operation is idempotent and no exception is thrown") {
                orderService.markOrderAsFailed("test_user_id_1", failedOrderId)
                failedOrder.status shouldBe OrderStatus.FAILED
            }
        }

        When("order status is PAID") {
            val paidOrder = Order.create("test_user_id_4", 1)
            paidOrder.markAsPaid()

            every {
                orderQueryRepository.findByOrderId("test_user_id_4", paidOrder.orderId)
            } returns paidOrder

            Then("IllegalStateException is thrown") {
                shouldThrow<IllegalStateException> {
                    orderService.markOrderAsFailed("test_user_id_4", paidOrder.orderId)
                }
            }
        }
    }

    Given("Find Orders by UserId Request") {
        val userId = "test_user_id_1"
        val order1 = Order.create(userId, 100)
        val order2 = Order.create(userId, 200)
        val orders = listOf(order1, order2)

        When("valid paging request is provided") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.DOWN
            )

            val pagingResponse = PagingResponse(
                items = orders,
                page = 1,
                limit = 20,
                nextCursor = null,
                prevCursor = null,
                hasNext = false,
                hasPrev = null,
                itemCount = 2
            )

            val mappedResponse = PagingResponse(
                items = orders.map { OrderResponse.from(it) },
                page = 1,
                limit = 20,
                nextCursor = null,
                prevCursor = null,
                hasNext = false,
                hasPrev = null,
                itemCount = 2
            )

            every {
                pagingService.paginate<Order>(any(), any())
            } returns pagingResponse

            every {
                pagingService.map<Order, OrderResponse>(any(), any())
            } answers {
                val response = firstArg<PagingResponse<Order>>()
                val mapper = secondArg<(Order) -> OrderResponse>()
                val items = response.items.map(mapper)
                PagingResponse(
                    items = items,
                    hasPrev = response.hasPrev,
                    hasNext = response.hasNext,
                    prevCursor = response.prevCursor,
                    nextCursor = response.nextCursor,
                    limit = response.limit,
                    page = response.page,
                    itemCount = response.itemCount
                )
            }

            val result = orderService.findOrdersByUserId(userId, request)

            Then("orders are returned successfully") {
                result.items.size shouldBe 2
                result.items[0].userId shouldBe userId
                result.items[1].userId shouldBe userId
                result.itemCount shouldBe 2
            }
        }

        When("cursor-based paging request is provided") {
            val request = BasePagingRequest(
                limit = 10,
                cursor = "some_cursor",
                direction = PagingDirection.DOWN
            )

            val pagingResponse = PagingResponse(
                items = orders,
                page = null,
                limit = 10,
                nextCursor = "next_cursor",
                prevCursor = null,
                hasNext = true,
                hasPrev = null,
                itemCount = 2
            )

            val mappedResponse = PagingResponse(
                items = orders.map { OrderResponse.from(it) },
                page = null,
                limit = 10,
                nextCursor = "next_cursor",
                prevCursor = null,
                hasNext = true,
                hasPrev = null,
                itemCount = 2
            )

            every {
                pagingService.paginate<Order>(any(), any())
            } returns pagingResponse

            every {
                pagingService.map<Order, OrderResponse>(any(), any())
            } answers {
                val response = firstArg<PagingResponse<Order>>()
                val mapper = secondArg<(Order) -> OrderResponse>()
                val items = response.items.map(mapper)
                PagingResponse(
                    items = items,
                    hasPrev = response.hasPrev,
                    hasNext = response.hasNext,
                    prevCursor = response.prevCursor,
                    nextCursor = response.nextCursor,
                    limit = response.limit,
                    page = response.page,
                    itemCount = response.itemCount
                )
            }

            val result = orderService.findOrdersByUserId(userId, request)

            Then("orders with cursor are returned successfully") {
                result.items.size shouldBe 2
                result.nextCursor shouldBe "next_cursor"
                result.hasNext shouldBe true
            }
        }

        When("empty result") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.DOWN
            )

            val emptyPagingResponse = PagingResponse(
                items = emptyList<Order>(),
                page = 1,
                limit = 20,
                nextCursor = null,
                prevCursor = null,
                hasNext = false,
                hasPrev = null,
                itemCount = 0
            )

            val emptyMappedResponse = PagingResponse(
                items = emptyList<OrderResponse>(),
                page = 1,
                limit = 20,
                nextCursor = null,
                prevCursor = null,
                hasNext = false,
                hasPrev = null,
                itemCount = 0
            )

            every {
                pagingService.paginate<Order>(any(), any())
            } returns emptyPagingResponse

            every {
                pagingService.map<Order, OrderResponse>(any(), any())
            } answers {
                val response = firstArg<PagingResponse<Order>>()
                val mapper = secondArg<(Order) -> OrderResponse>()
                val items = response.items.map(mapper)
                PagingResponse(
                    items = items,
                    hasPrev = response.hasPrev,
                    hasNext = response.hasNext,
                    prevCursor = response.prevCursor,
                    nextCursor = response.nextCursor,
                    limit = response.limit,
                    page = response.page,
                    itemCount = response.itemCount
                )
            }

            val result = orderService.findOrdersByUserId(userId, request)

            Then("empty list is returned") {
                result.items.isEmpty() shouldBe true
                result.itemCount shouldBe 0
            }
        }
    }
})