package com.cafe.quiz.feature.order.entity

import com.cafe.quiz.feature.order.model.OrderStatus
import com.cafe.quiz.support.generator.IdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    val orderId: Long = IdGenerator.generate(),
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 10, nullable = false)
    var status: OrderStatus = OrderStatus.READY,
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isCancelable() = status == OrderStatus.PAID

    fun isFailed() = status == OrderStatus.FAILED
}
