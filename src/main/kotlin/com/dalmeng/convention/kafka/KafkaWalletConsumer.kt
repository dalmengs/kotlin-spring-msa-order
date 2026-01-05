package com.dalmeng.convention.kafka

import com.dalmeng.convention.kafka.event.WalletDebitedEvent
import com.dalmeng.convention.order.service.OrderService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class KafkaWalletConsumer(
    private val objectMapper: ObjectMapper,
    private val orderService: OrderService,
) {

    @KafkaListener(
        topics = ["wallet.debited"],
        groupId = "order-service"
    )
    fun consume(message: String) {
        try {
            val event = objectMapper.readValue(
                message,
                WalletDebitedEvent::class.java
            )

            orderService.markOrderAsPaid(event.userId, event.orderId)

        } catch (e: Exception) {
            throw e
        }
    }
}