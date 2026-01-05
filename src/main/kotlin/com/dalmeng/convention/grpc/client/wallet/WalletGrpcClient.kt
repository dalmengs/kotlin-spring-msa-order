package com.dalmeng.convention.grpc.client.wallet

import com.example.wallet.grpc.DecreaseWalletRequest
import com.example.wallet.grpc.DecreaseWalletResponse
import com.example.wallet.grpc.WalletServiceGrpcKt
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class WalletGrpcClient {

    @GrpcClient("wallet")
    lateinit var stub: WalletServiceGrpcKt.WalletServiceCoroutineStub

    suspend fun decrease(
        orderId: String,
        userId: String,
        amount: Long
    ): DecreaseWalletResponse {
        val request = DecreaseWalletRequest.newBuilder()
            .setOrderId(orderId)
            .setUserId(userId)
            .setAmount(amount)
            .build()

        return stub.decreaseWallet(request)
    }
}