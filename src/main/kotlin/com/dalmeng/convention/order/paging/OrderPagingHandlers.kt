package com.dalmeng.convention.order.paging

import com.dalmeng.convention.common.paging.PagingHandlers
import com.dalmeng.convention.order.entity.Order
import com.dalmeng.convention.order.exception.OrderNotFoundException
import com.dalmeng.convention.order.repository.OrderQueryRepository
import com.dalmeng.convention.order.repository.OrderRepository
import org.springframework.data.domain.Pageable

class OrderPagingHandlers(
    private val userId: String,
    private val orderRepository: OrderRepository,
    private val orderQueryRepository: OrderQueryRepository,
) : PagingHandlers<Order> {

    override fun getCursorFromResponse(data: Order): String {
        return data.id
    }

    override fun findById(id: String): Order {
        val order = orderQueryRepository.findById(id)
            ?: throw OrderNotFoundException()
        
        // userId 검증
        if (order.userId != userId) {
            throw OrderNotFoundException()
        }
        
        return order
    }

    override fun findAllByOrderByIdAsc(pageable: Pageable): List<Order> {
        return orderRepository.findAllByUserIdOrderBySeqAsc(userId, pageable)
    }

    override fun findAllByOrderByIdDesc(pageable: Pageable): List<Order> {
        return orderRepository.findAllByUserIdOrderBySeqDesc(userId, pageable)
    }

    override fun findAllByIdGreaterThanOrderByIdAsc(id: String, pageable: Pageable): List<Order> {
        val order = orderQueryRepository.findById(id)
            ?: throw OrderNotFoundException()
        
        // userId 검증
        if (order.userId != userId) {
            throw OrderNotFoundException()
        }
        
        return orderRepository.findAllByUserIdAndSeqGreaterThanOrderBySeqAsc(
            userId, order.seq, pageable
        )
    }

    override fun findAllByIdLessThanOrderByIdDesc(id: String, pageable: Pageable): List<Order> {
        val order = orderQueryRepository.findById(id)
            ?: throw OrderNotFoundException()
        
        // userId 검증
        if (order.userId != userId) {
            throw OrderNotFoundException()
        }
        
        return orderRepository.findAllByUserIdAndSeqLessThanOrderBySeqDesc(
            userId, order.seq, pageable
        )
    }
}

