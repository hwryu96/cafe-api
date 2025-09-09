package com.cafe.quiz.feature.order.repository

import com.cafe.quiz.feature.order.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderEntityRepository : JpaRepository<OrderEntity, Long> {
    fun findByMemberIdAndOrderId(
        memberId: Long,
        orderId: Long,
    ): OrderEntity?
}
