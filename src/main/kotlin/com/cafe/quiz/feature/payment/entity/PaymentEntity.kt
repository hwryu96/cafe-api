package com.cafe.quiz.feature.payment.entity

import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.support.generator.IdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    val id: Long = IdGenerator.generate(),
    @Column(name = "order_id", nullable = false)
    val orderId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 10, nullable = false)
    val status: PaymentStatus,
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
