package com.dalmeng.convention.kafka.event

data class WalletDebitedEvent(
    val orderId: String,
    val userId: String,
)
