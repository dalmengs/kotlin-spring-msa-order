package com.dalmeng.convention.order.repository

import com.dalmeng.convention.order.entity.Order
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserIdOrderBySeqAsc(userId: String, pageable: Pageable): List<Order>
    fun findAllByUserIdOrderBySeqDesc(userId: String, pageable: Pageable): List<Order>
    fun findAllByUserIdAndSeqGreaterThanOrderBySeqAsc(userId: String, seq: Long, pageable: Pageable): List<Order>
    fun findAllByUserIdAndSeqLessThanOrderBySeqDesc(userId: String, seq: Long, pageable: Pageable): List<Order>
}

