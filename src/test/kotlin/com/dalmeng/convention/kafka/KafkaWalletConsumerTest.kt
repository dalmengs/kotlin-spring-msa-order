package com.dalmeng.convention.kafka

import com.dalmeng.convention.kafka.event.WalletDebitedEvent
import com.dalmeng.convention.order.service.OrderService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import tools.jackson.databind.ObjectMapper

class KafkaWalletConsumerTest : BehaviorSpec({
    val objectMapper = ObjectMapper()
    val orderService = mockk<OrderService>()

    val kafkaWalletConsumer = KafkaWalletConsumer(
        objectMapper = objectMapper,
        orderService = orderService
    )

    Given("Consume Wallet Debited Event") {
        val userId = "test_user_id_1"
        val orderId = "test_order_id_1"
        val event = WalletDebitedEvent(
            orderId = orderId,
            userId = userId
        )

        When("valid event message is consumed") {
            val message = objectMapper.writeValueAsString(event)

            every {
                orderService.markOrderAsPaid(userId, orderId)
            } returns Unit

            kafkaWalletConsumer.consume(message)

            Then("order is marked as paid") {
                verify(exactly = 1) {
                    orderService.markOrderAsPaid(userId, orderId)
                }
            }
        }

        When("invalid JSON message is consumed") {
            val invalidMessage = "invalid json"

            Then("exception is thrown") {
                shouldThrow<Exception> {
                    kafkaWalletConsumer.consume(invalidMessage)
                }
            }
        }

        When("message with missing fields is consumed") {
            val invalidMessage = """{"orderId": "test_order_id_1"}"""

            Then("exception is thrown") {
                shouldThrow<Exception> {
                    kafkaWalletConsumer.consume(invalidMessage)
                }
            }
        }

        When("valid event with different orderId and userId is consumed") {
            val differentEvent = WalletDebitedEvent(
                orderId = "test_order_id_2",
                userId = "test_user_id_2"
            )
            val message = objectMapper.writeValueAsString(differentEvent)

            every {
                orderService.markOrderAsPaid("test_user_id_2", "test_order_id_2")
            } returns Unit

            kafkaWalletConsumer.consume(message)

            Then("order with different IDs is marked as paid") {
                verify(exactly = 1) {
                    orderService.markOrderAsPaid("test_user_id_2", "test_order_id_2")
                }
            }
        }

        When("orderService throws exception") {
            val message = objectMapper.writeValueAsString(event)

            every {
                orderService.markOrderAsPaid(userId, orderId)
            } throws RuntimeException("Order not found")

            Then("exception is propagated") {
                shouldThrow<RuntimeException> {
                    kafkaWalletConsumer.consume(message)
                }
            }
        }
    }
})

