package com.dalmeng.convention.grpc.client.wallet

import com.example.wallet.grpc.DecreaseWalletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class WalletService(
    private val walletGrpcClient: WalletGrpcClient
) {
    fun decreaseBalance(userId: String, orderId: String, amount: Long): DecreaseWalletResponse =
        runBlocking {
            walletGrpcClient.decrease(
                orderId = orderId,
                userId = userId,
                amount = amount,
            )
        }
}